package com.workflowbuilder.frontend;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.workflowbuilder.frontend.service.MicroserviceClientFactory;
import org.junit.jupiter.api.Test;

class FrontendServiceApplicationTest {

    @Test
    void MicroserviceClientFactoryCreatesClients() {
        MicroserviceClientFactory service = new MicroserviceClientFactory(
                "http://localhost:8081",
                "http://localhost:8082",
                "http://localhost:8083"
        );

        assertNotNull(service.workflowClient());
        assertNotNull(service.executionClient());
        assertNotNull(service.monitoringClient());
    }
}
