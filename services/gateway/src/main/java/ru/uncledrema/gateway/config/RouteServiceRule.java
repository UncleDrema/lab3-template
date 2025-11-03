package ru.uncledrema.gateway.config;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class RouteServiceRule {
    private boolean critical = false;
    private String fallback; // JSON или null
    private boolean enqueueOnFailure = false;
}
