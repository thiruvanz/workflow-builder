package com.workflowbuilder.frontend.api;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.workflowbuilder.frontend.FrontendServiceApplication;
import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = FrontendServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
class FrontendApiControllerIT {

    static MockWebServer backend;

    @BeforeAll
    static void startBackend() throws IOException {
        backend = new MockWebServer();
        backend.start();
    }

    @AfterAll
    static void stopBackend() throws IOException {
        backend.shutdown();
    }

    @DynamicPropertySource
    static void backendUrls(DynamicPropertyRegistry r) {
        String base = backend.url("/").toString();
        r.add("workflow.service.base-url", () -> base);
        r.add("execution.service.base-url", () -> base);
        r.add("monitoring.service.base-url", () -> base);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void listWorkflowsProxiesToWorkflowService() throws Exception {
        backend.enqueue(new MockResponse()
                .setBody("[{\"id\":1,\"name\":\"demo\"}]")
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/ui-api/workflows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("demo"));
    }

    @Test
    void dashboardProxiesToMonitoring() throws Exception {
        backend.enqueue(new MockResponse()
                .setBody("{\"totalWorkflows\":1,\"publishedWorkflows\":1,\"totalRuns\":2}")
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/ui-api/monitoring/dashboard"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalRuns").value(2));
    }

    @Test
    void createWorkflowPostsToDownstream() throws Exception {
        backend.enqueue(new MockResponse()
                .setBody("{\"id\":5,\"name\":\"new\"}")
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(
                        post("/ui-api/workflows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"name\":\"new\",\"definitionJson\":\"{}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void updateWorkflowPutsToDownstream() throws Exception {
        backend.enqueue(new MockResponse()
                .setBody("{\"id\":7,\"name\":\"x\"}")
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(
                        put("/ui-api/workflows/7")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"definitionJson\":\"{}\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));
    }

    @Test
    void triggerPostsToWorkflowExecutionService() throws Exception {
        backend.enqueue(new MockResponse()
                .setBody("{\"id\":9,\"workflowId\":1}")
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(
                        post("/ui-api/executions/trigger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"workflowId\":1,\"tenantId\":\"t\",\"idempotencyKey\":\"k\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowId").value(1));
    }
}
