package com.workflowbuilder.discovery;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(
        properties = {
            "eureka.client.register-with-eureka=false",
            "eureka.client.fetch-registry=false",
            "spring.application.name=discovery-it"
        })
@TestPropertySource(properties = "server.port=0")
class DiscoveryServiceApplicationIT {

    @Test
    void contextLoads() {
        // Eureka server starts with embedded servlet container
    }
}
