package ru.uncledrema.gateway.config;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
@ConfigurationProperties(prefix = "degradation")
@Getter
@Setter
public class DegradationProperties {
    private Map<String, ServiceRule> services = new HashMap<>();
    // Теперь routes — список, чтобы сохранить точные path'ы с символами '/'
    private List<RouteRule> routes = new ArrayList<>();

    // вспомогательная таблица для быстрого поиска: хранит и оригинальные пути, и нормализованные ключи
    private final Map<String, RouteRule> routeByPath = new ConcurrentHashMap<>();

    public ServiceRule getServiceRule(String key) {
        return services.getOrDefault(key, new ServiceRule());
    }

    public RouteRule getRouteRuleByPath(HttpMethod method, String path) {
        if (path == null) return new RouteRule();
        return routes.stream().peek(r -> {
            log.info("Lookup: {} {} ~= {} {}, starts: {}", r.getMethod(), r.getPath(), method.name(), path, path.startsWith(r.getPath()));
                }).filter(r -> Objects.equals(r.getMethod(), method.name()) && path.startsWith(r.getPath()))
                .findFirst()
                .orElseGet(() -> {
                    log.warn("route path {} not found", path);
                    return new RouteRule();
                });
    }

    @PostConstruct
    private void init() {
        routeByPath.clear();
        if (routes == null) return;
        for (RouteRule r : routes) {
            if (r == null) continue;
            String p = r.getPath();
            if (p == null) continue;
            routeByPath.put(p, r); // exact path
        }
        log.info("Degradation routes loaded: {}", routeByPath.keySet());
    }
}