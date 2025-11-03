package ru.uncledrema.gateway.web;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestTemplate;
import ru.uncledrema.gateway.config.DegradationProperties;
import ru.uncledrema.gateway.config.RouteRule;
import ru.uncledrema.gateway.config.RouteServiceRule;
import ru.uncledrema.gateway.dto.ErrorDto;
import ru.uncledrema.gateway.dto.PrivilegeShortInfoDto;
import ru.uncledrema.gateway.dto.TicketDto;
import ru.uncledrema.gateway.services.CircuitBreakerService;
import ru.uncledrema.gateway.services.PrivilegeClient;
import ru.uncledrema.gateway.services.TicketClient;
import ru.uncledrema.gateway.dto.UserInfoDto;

import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.Supplier;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
@Slf4j
public class ProxyController {
    private final RestTemplate restTemplate;
    private final PrivilegeClient privilegeClient;
    private final TicketClient ticketClient;
    private final DegradationProperties degradationProperties;
    private final CircuitBreakerService circuitBreakerService;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${downstream.flights:http://localhost:8060}")
    private String flightsBase;

    @Value("${downstream.tickets:http://localhost:8070}")
    private String ticketsBase;

    @Value("${downstream.privileges:http://localhost:8050}")
    private String privilegesBase;

    // очередь для повторной отправки запросов
    private final BlockingQueue<RequestTask> retryQueue = new LinkedBlockingQueue<>();
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    // инициализация фоновой обработки очереди
    {
        // запускаем фонового воркера, который раз в секунду пытается обработать одну задачу из очереди
        scheduler.scheduleWithFixedDelay(this::processQueueOnce, 0, 1, TimeUnit.SECONDS);
    }

    // ---- Aggregation example: GET /me ----
    @GetMapping("/me")
    public ResponseEntity<?> me(@RequestHeader(value = "X-User-Name") String username) {
        String routeKey = "/api/v1/me";

        // Пример использования общего helper'а: возвращает либо значение, либо отметку о критичном недоступности, либо фолбек
        ServiceResult<PrivilegeShortInfoDto> privRes = getWithDegradation(
                routeKey,
                "privileges",
                () -> privilegeClient.getPrivilegeForUser(username).orElse(null),
                new TypeReference<>() {}
        );

        if (privRes.criticalUnavailable) {
            String msg = getServiceDisplayName("privileges") + " unavailable";
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorDto(msg));
        }

        ServiceResult<List<TicketDto>> ticketsRes = getWithDegradation(
                routeKey,
                "tickets",
                () -> ticketClient.getTickets(username).orElse(null),
                new TypeReference<>() {}
        );

        if (ticketsRes.criticalUnavailable) {
            String msg = getServiceDisplayName("tickets") + " unavailable";
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(new ErrorDto(msg));
        }

        UserInfoDto dto = new UserInfoDto(ticketsRes.value, privRes.value);
        return ResponseEntity.ok(dto);
    }

    @RequestMapping(path = "/**", method = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.PATCH})
    public ResponseEntity<?> proxyAll(HttpServletRequest request, @RequestHeader(value = "X-Service-Key", required = false) String service, @RequestBody(required = false) byte[] body) {
        HttpMethod method = HttpMethod.valueOf(request.getMethod());
        String fullPath = request.getRequestURI(); // /api/v1/...
        String prefix = "/api/v1";
        String forwardPath = fullPath.startsWith(prefix) ? fullPath.substring(prefix.length()) : fullPath; // /flights/...
        String query = request.getQueryString();
        String targetBase = selectTarget(forwardPath);
        String serviceKey = service == null ? serviceKeyFor(forwardPath) : service;
        String routeKey = prefix + forwardPath; // предполагаем, что в конфиге ключи маршрутов заданы как "/api/v1/..."
        log.info("proxy: {} {} -> serviceKey={} target={} for {}", method.name(), forwardPath, serviceKey, targetBase, serviceKey);

        // если нет целевого сервиса (не flights/tickets/privilege) — 404
        if (targetBase == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(("No downstream service for path: " + forwardPath).getBytes(StandardCharsets.UTF_8));
        }

        RouteRule routeRule = degradationProperties.getRouteRuleByPath(method, routeKey);
        RouteServiceRule routeServiceRule = routeRule.getRuleFor(serviceKey);


        // Если цепь открыта — решаем по правилу маршрута
        try {
            circuitBreakerService.beforeCall(serviceKey);
        } catch (CircuitOpenException ignored) {
            if (routeServiceRule.isCritical()) {
                String msg = getServiceDisplayName(serviceKeyFor(forwardPath)) + " unavailable";
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body((new ErrorDto(msg)));
            } else {
                String fallback = routeServiceRule.getFallback();
                byte[] bodyBytes = fallback != null ? fallback.getBytes(StandardCharsets.UTF_8) : new byte[0];
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(bodyBytes, headers, HttpStatus.OK);
            }
        }

        String url = targetBase + forwardPath + (query != null ? "?" + query : "");
        log.info("redirecting to URL: {}", url);

        HttpHeaders headers = copyRequestHeaders(request);

        HttpEntity<byte[]> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<byte[]> resp = restTemplate.exchange(URI.create(url), method, entity, byte[].class);
            circuitBreakerService.recordSuccess(serviceKey);
            HttpHeaders outHeaders = filterResponseHeaders(resp.getHeaders());
            return new ResponseEntity<>(resp.getBody(), outHeaders, resp.getStatusCode());
        } catch (HttpStatusCodeException ex) {
            log.warn("status code exception {} from downstream: {}", ex.getClass().getCanonicalName(), ex.getStatusCode());
            circuitBreakerService.recordFailure(serviceKey);
            byte[] respBody = ex.getResponseBodyAsByteArray();
            HttpHeaders outHeaders = filterResponseHeaders(ex.getResponseHeaders());
            return new ResponseEntity<>(respBody, outHeaders, ex.getStatusCode());
        } catch (Exception ignored) {
            log.warn("exception from downstream: {}, rule: {}", ignored.getMessage(), routeServiceRule);
            circuitBreakerService.recordFailure(serviceKey);
            if (!routeServiceRule.isCritical() && routeServiceRule.isEnqueueOnFailure()) {
                RequestTask task = new RequestTask(url, method, copyRequestHeaders(request), body, serviceKey);
                retryQueue.offer(task);
                String fallback = routeServiceRule.getFallback();
                byte[] bodyBytes = fallback != null ? fallback.getBytes(StandardCharsets.UTF_8) : new byte[0];
                HttpHeaders h = new HttpHeaders();
                h.setContentType(MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(bodyBytes, h, HttpStatus.OK);
            }

            if (routeServiceRule.isCritical()) {
                String msg = getServiceDisplayName(serviceKeyFor(forwardPath)) + " unavailable";
                return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(new ErrorDto(msg));
            } else {
                String fallback = routeServiceRule.getFallback();
                byte[] bodyBytes = fallback != null ? fallback.getBytes(StandardCharsets.UTF_8) : new byte[0];
                HttpHeaders h = new HttpHeaders();
                h.setContentType(MediaType.APPLICATION_JSON);
                return new ResponseEntity<>(bodyBytes, h, HttpStatus.OK);
            }
        }
    }

    // перегрузка для простых типов / конкретных классов
    private <T> ServiceResult<T> getWithDegradation(String routeKey, String serviceKey, Supplier<T> supplier, TypeReference<T> typeRef) {
        RouteRule routeRule = degradationProperties.getRouteRuleByPath(HttpMethod.GET, routeKey);
        RouteServiceRule routeServiceRule = routeRule.getRuleFor(serviceKey);

        try {
            circuitBreakerService.beforeCall(serviceKey);
            T val = supplier.get();
            circuitBreakerService.recordSuccess(serviceKey);
            return ServiceResult.ok(val);
        } catch (CircuitOpenException _) {
            if (routeServiceRule.isCritical()) {
                return ServiceResult.criticalUnavailable();
            } else {
                circuitBreakerService.recordFailure(serviceKey);
                String fallback = routeServiceRule.getFallback();
                if (fallback == null || fallback.isBlank()) return ServiceResult.fallback(null);
                try {
                    T parsed = objectMapper.readValue(fallback, typeRef);
                    return ServiceResult.fallback(parsed);
                } catch (Exception e) {
                    log.warn("Failed to parse fallback for {}/{}: {}", routeKey, serviceKey, e.getMessage());
                    return ServiceResult.fallback(null);
                }
            }
        } catch (Exception ignored) {
            circuitBreakerService.recordFailure(serviceKey);
            if (routeServiceRule.isCritical()) {
                return ServiceResult.criticalUnavailable();
            } else {
                String fallback = routeServiceRule.getFallback();
                if (fallback == null || fallback.isBlank()) return ServiceResult.fallback(null);
                try {
                    T parsed = objectMapper.readValue(fallback, typeRef);
                    return ServiceResult.fallback(parsed);
                } catch (Exception e) {
                    log.warn("Failed to parse fallback for {}/{}: {}", routeKey, serviceKey, e.getMessage());
                    return ServiceResult.fallback(null);
                }
            }
        }
    }

    // фоновой процессинг: пробуем обработать одну задачу из очереди
    private void processQueueOnce() {
        RequestTask task = retryQueue.poll();
        if (task == null) return;
        log.info("Executing task {} with url {}", task.serviceKey, task.url);
        try {
            HttpEntity<byte[]> entity = new HttpEntity<>(task.body, task.headers);
            restTemplate.exchange(URI.create(task.url), task.method, entity, byte[].class);
            circuitBreakerService.recordSuccess(task.serviceKey);
            log.info("Retry task succeeded for url={}, service={}", task.url, task.serviceKey);
            // при успехе задача просто не возвращаем в очередь
        } catch (Exception e) {
            log.warn("Retry task failed for url={}, service={}, will retry in 5s: {}", task.url, task.serviceKey, e.getMessage());
            circuitBreakerService.recordFailure(task.serviceKey);
            // переотправляем задачу через 5 секунд
            scheduler.schedule(() -> retryQueue.offer(task), 5, TimeUnit.SECONDS);
        }
    }

    // ---- небольшой вспомогательный класс для результата ----
    private static class ServiceResult<T> {
        final boolean criticalUnavailable;
        final boolean usedFallback;
        final T value;

        private ServiceResult(boolean criticalUnavailable, boolean usedFallback, T value) {
            this.criticalUnavailable = criticalUnavailable;
            this.usedFallback = usedFallback;
            this.value = value;
        }

        static <T> ServiceResult<T> ok(T v) { return new ServiceResult<>(false, false, v); }
        static <T> ServiceResult<T> fallback(T v) { return new ServiceResult<>(false, true, v); }
        static <T> ServiceResult<T> criticalUnavailable() { return new ServiceResult<>(true, false, null); }
    }

    // ---- вспомогательный класс для тасков очереди ----
    private static class RequestTask {
        final String url;
        final HttpMethod method;
        final HttpHeaders headers;
        final byte[] body;
        final String serviceKey;

        RequestTask(String url, HttpMethod method, HttpHeaders headers, byte[] body, String serviceKey) {
            this.url = url;
            this.method = method;
            this.headers = headers;
            this.body = body;
            this.serviceKey = serviceKey;
        }
    }

    // ---- утилиты копирования заголовков и маршрутизации ----
    private String serviceKeyFor(String forwardPath) {
        String p = forwardPath.toLowerCase();
        if (p.startsWith("/flights")) return "flights";
        if (p.startsWith("/tickets")) return "tickets";
        if (p.startsWith("/privilege") || p.startsWith("/privileges")) return "privileges";
        return "unknown";
    }

    @Nullable
    private String selectTarget(String forwardPath) {
        String p = forwardPath.toLowerCase();
        if (p.startsWith("/flights")) return flightsBase;
        if (p.startsWith("/tickets")) return ticketsBase;
        if (p.startsWith("/privilege") || p.startsWith("/privileges")) return privilegesBase;
        return null;
    }

    private String getServiceDisplayName(String serviceKey) {
        var rule = degradationProperties.getServiceRule(serviceKey);
        if (rule != null && rule.getDisplayName() != null && !rule.getDisplayName().isBlank()) {
            return rule.getDisplayName();
        }
        // fallback: capitalize serviceKey
        if (serviceKey == null) return "Service";
        String s = serviceKey.trim();
        if (s.isEmpty()) return "Service";
        return Character.toUpperCase(s.charAt(0)) + s.substring(1);
    }

    private HttpHeaders copyRequestHeaders(HttpServletRequest request) {
        HttpHeaders headers = new HttpHeaders();
        Enumeration<String> names = request.getHeaderNames();
        while (names != null && names.hasMoreElements()) {
            String name = names.nextElement();
            if ("host".equalsIgnoreCase(name) || "content-length".equalsIgnoreCase(name)) continue;
            Enumeration<String> values = request.getHeaders(name);
            while (values.hasMoreElements()) {
                headers.add(name, values.nextElement());
            }
        }
        return headers;
    }

    private HttpHeaders filterResponseHeaders(HttpHeaders in) {
        HttpHeaders out = new HttpHeaders();
        if (in == null) return out;
        in.forEach((k, v) -> {
            if ("transfer-encoding".equalsIgnoreCase(k) || "connection".equalsIgnoreCase(k)) return;
            out.put(k, v);
        });
        return out;
    }
}
