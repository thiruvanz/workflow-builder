package com.workflowbuilder.gateway.config;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

class SecurityConfigTest {

    @Test
    void jwtDecoderBeanCanBeCreated() {
        SecurityConfig config = new SecurityConfig();
        assertNotNull(config.jwtDecoder());
    }
}
