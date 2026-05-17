package com.workflowbuilder.bff;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.workflowbuilder.resilience.OutboundCallGuard;
import com.workflowbuilder.resilience.saga.SagaRunner;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

class BffServiceApplicationTest {

    @Test
    void applicationClassLoads() {
        assertEquals("com.workflowbuilder.bff.BffServiceApplication", BffServiceApplication.class.getName());
    }

    @Test
    void bffControllerCanBeConstructed() {
        BffApiController controller = new BffApiController(
                RestClient.builder(),
                "http://localhost:8081",
                "http://localhost:8083",
                new OutboundCallGuard(CircuitBreakerRegistry.ofDefaults()),
                new SagaRunner());
        assertEquals(BffApiController.class, controller.getClass());
    }
}
