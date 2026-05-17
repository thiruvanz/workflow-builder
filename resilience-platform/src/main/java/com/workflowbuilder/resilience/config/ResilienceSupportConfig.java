package com.workflowbuilder.resilience.config;

import com.workflowbuilder.resilience.OutboundCallGuard;
import com.workflowbuilder.resilience.saga.SagaRunner;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ResilienceSupportConfig {

    @Bean
    @ConditionalOnMissingBean(OutboundCallGuard.class)
    public OutboundCallGuard outboundCallGuard(
            @Autowired(required = false) CircuitBreakerRegistry registry) {
        CircuitBreakerRegistry reg = registry != null ? registry : CircuitBreakerRegistry.ofDefaults();
        return new OutboundCallGuard(reg);
    }

    @Bean
    public SagaRunner sagaRunner() {
        return new SagaRunner();
    }
}
