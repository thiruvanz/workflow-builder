package com.workflowbuilder.resilience;

import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.function.Supplier;

/**
 * Wraps outbound HTTP (or any) calls with a named Resilience4j circuit breaker.
 */
public final class OutboundCallGuard {

    private final CircuitBreakerRegistry registry;

    public OutboundCallGuard(CircuitBreakerRegistry registry) {
        this.registry = registry;
    }

    public <T> T execute(String circuitName, Supplier<T> supplier) {
        return registry.circuitBreaker(circuitName).executeSupplier(supplier);
    }

    public void executeVoid(String circuitName, Runnable runnable) {
        registry.circuitBreaker(circuitName).executeRunnable(runnable);
    }
}
