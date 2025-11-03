package ru.uncledrema.gateway.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
@Setter
@ToString
public class RouteRule {
    // сохраняем оригинальный path точно как в YAML ("/api/v1/me")
    private String path;
    private String method; // GET, POST и т.д.
    // поведение по сервисам внутри маршрута
    private Map<String, RouteServiceRule> services = new HashMap<>();

    public RouteServiceRule getRuleFor(String serviceKey) {
        if (!services.containsKey(serviceKey)) {
            log.warn("serviceKey {} not found", serviceKey);
        }
        return services.getOrDefault(serviceKey, new RouteServiceRule());
    }
}
