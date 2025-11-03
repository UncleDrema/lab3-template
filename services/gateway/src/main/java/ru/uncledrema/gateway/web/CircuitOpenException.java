package ru.uncledrema.gateway.web;

import ru.uncledrema.gateway.config.DegradationRule;

public class CircuitOpenException extends RuntimeException {
    public CircuitOpenException() {
        super("Circuit is open");
    }
}
