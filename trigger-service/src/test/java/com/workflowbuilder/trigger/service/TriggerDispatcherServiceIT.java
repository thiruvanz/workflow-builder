package com.workflowbuilder.trigger.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.workflowbuilder.trigger.TriggerServiceApplication;
import com.workflowbuilder.trigger.api.TriggerRequest;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(classes = TriggerServiceApplication.class)
@ActiveProfiles("integrationtest")
class WebhookDispatcherServiceIT {

    static MockWebServer executionApi;

    @BeforeAll
    static void startExecutionMock() throws IOException {
        executionApi = new MockWebServer();
        executionApi.start();
    }

    @AfterAll
    static void stopExecutionMock() throws IOException {
        executionApi.shutdown();
    }

    @DynamicPropertySource
    static void executionUrl(DynamicPropertyRegistry r) {
        r.add("execution.service.base-url", () -> executionApi.url("/").toString());
    }

    @Autowired
    WebhookDispatcherService dispatcherService;

    @Test
    void dispatchPostsTriggerPayloadToWorkflowExecutionService() throws InterruptedException {
        executionApi.enqueue(new MockResponse().setResponseCode(204));

        dispatcherService.dispatch(new TriggerRequest(42L, "tenant-x", "idem-123"));

        RecordedRequest req = executionApi.takeRequest();
        assertEquals("POST", req.getMethod());
        assertEquals("/api/executions/trigger", req.getPath());
        String body = req.getBody().readUtf8();
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("\"workflowId\":42"));
        org.junit.jupiter.api.Assertions.assertTrue(body.contains("tenant-x"));
    }
}
