package com.workflowbuilder.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        properties = {
            "eureka.client.enabled=false",
            "spring.cloud.discovery.enabled=false",
            "spring.application.name=gateway-it",
            "spring.main.web-application-type=reactive"
        })
@TestPropertySource(properties = "server.port=0")
class GatewayServiceApplicationIT {

    @Test
    void contextLoads() {
        // Gateway + Security (JWT) reactive stack
    }
}
