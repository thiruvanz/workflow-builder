package com.workflowbuilder.gateway.config;

import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.UUID;

@Configuration
public class TraceHeaderConfig {
    @Bean
    public GlobalFilter traceHeaderFilter() {
        return (exchange, chain) -> {
            String traceId = exchange.getRequest().getHeaders().getFirst("X-Trace-Id");
            if (traceId == null || traceId.isBlank()) {
                traceId = UUID.randomUUID().toString();
            }
            final String finalTraceId = traceId;
            exchange.getResponse().getHeaders().set("X-Trace-Id", finalTraceId);
            return chain.filter(exchange);
        };
    }
}
