package ru.uncledrema.gateway.config;

public class DegradationRule {
    private boolean critical = false;
    private String fallback; // свободный JSON / строка для ответа
    private int failureThreshold = 3;
    private long resetTimeoutMs = 30_000L;

    public boolean isCritical() { return critical; }
    public void setCritical(boolean critical) { this.critical = critical; }

    public String getFallback() { return fallback; }
    public void setFallback(String fallback) { this.fallback = fallback; }

    public int getFailureThreshold() { return failureThreshold; }
    public void setFailureThreshold(int failureThreshold) { this.failureThreshold = failureThreshold; }

    public long getResetTimeoutMs() { return resetTimeoutMs; }
    public void setResetTimeoutMs(long resetTimeoutMs) { this.resetTimeoutMs = resetTimeoutMs; }
}