package ru.uncledrema.gateway.services;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.uncledrema.gateway.config.DegradationProperties;
import ru.uncledrema.gateway.config.DegradationRule;
import ru.uncledrema.gateway.config.ServiceRule;
import ru.uncledrema.gateway.web.CircuitOpenException;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class CircuitBreakerService {
    private final DegradationProperties props;
    private final RestTemplate restTemplate;

    private final Map<String, AtomicInteger> failures = new ConcurrentHashMap<>();
    private final Map<String, Long> openUntil = new ConcurrentHashMap<>();
    private final ScheduledExecutorService probes = Executors.newScheduledThreadPool(1);
    private final Map<String, ScheduledFuture<?>> probeTasks = new ConcurrentHashMap<>();

    public CircuitBreakerService(DegradationProperties props, RestTemplate restTemplate) {
        this.props = props;
        this.restTemplate = restTemplate;
    }

    public void beforeCall(String serviceKey) {
        if (isOpen(serviceKey)) {
            throw new CircuitOpenException();
        }
    }

    public boolean isOpen(String serviceKey) {
        Long until = openUntil.get(serviceKey);
        if (until == null) return false;
        if (System.currentTimeMillis() > until) {
            // время ожидания истекло — переводим в полузакрытое состояние: продолжим позволять проверки,
            // но не автоматом закрываем — следующая удача закроет цепь.
            openUntil.remove(serviceKey);
            return false;
        }
        return true;
    }

    public void recordSuccess(String serviceKey) {
        failures.remove(serviceKey);
        openUntil.remove(serviceKey);
        ScheduledFuture<?> f = probeTasks.remove(serviceKey);
        if (f != null) f.cancel(true);
    }

    public void recordFailure(String serviceKey) {
        ServiceRule rule = props.getServiceRule(serviceKey);
        AtomicInteger cur = failures.computeIfAbsent(serviceKey, k -> new AtomicInteger(0));
        int v = cur.incrementAndGet();
        if (v >= rule.getFailureThreshold()) {
            long until = System.currentTimeMillis() + rule.getResetTimeoutMs();
            openUntil.put(serviceKey, until);
            scheduleProbing(serviceKey, rule);
        }
    }

    private void scheduleProbing(String serviceKey, ServiceRule rule) {
        probeTasks.computeIfAbsent(serviceKey, k -> probes.scheduleAtFixedRate(() -> {
            try {
                String healthUrl = rule.getHealthUrl();
                if (healthUrl == null) return;
                ResponseEntity<String> r = restTemplate.getForEntity(healthUrl, String.class);
                if (r.getStatusCode().is2xxSuccessful()) {
                    // сервис вернулся — закрываем цепь
                    recordSuccess(serviceKey);
                }
            } catch (Exception ignored) {
                // продолжать пробовать
            }
        }, rule.getProbeIntervalMs(), rule.getProbeIntervalMs(), TimeUnit.MILLISECONDS));
    }

    // optional: force-close or shutdown
    public void shutdown() {
        probes.shutdownNow();
    }
}