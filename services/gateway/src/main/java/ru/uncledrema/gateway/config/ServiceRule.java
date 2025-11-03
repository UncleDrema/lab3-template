package ru.uncledrema.gateway.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ServiceRule {
    private String healthUrl; // полный URL например: http://localhost:8060/manage/health
    private int failureThreshold = 3;
    private long resetTimeoutMs = 30_000L;
    private long probeIntervalMs = 5_000L;
    // Отображаемое имя сервиса для ошибок (например: "Bonus Service")
    private String displayName;
}
