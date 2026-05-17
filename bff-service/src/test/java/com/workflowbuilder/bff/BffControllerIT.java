package com.workflowbuilder.bff;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(classes = BffServiceApplication.class)
@AutoConfigureMockMvc
@ActiveProfiles("integrationtest")
class BffControllerIT {

    static MockWebServer backend;

    @BeforeAll
    static void start() throws IOException {
        backend = new MockWebServer();
        backend.start();
    }

    @AfterAll
    static void stop() throws IOException {
        backend.shutdown();
    }

    @DynamicPropertySource
    static void urls(DynamicPropertyRegistry r) {
        String base = backend.url("/").toString();
        r.add("workflow.service.base-url", () -> base);
        r.add("monitoring.service.base-url", () -> base);
    }

    @Autowired
    MockMvc mockMvc;

    @Test
    void overviewAggregatesWorkflowsAndDashboard() throws Exception {
        backend.enqueue(new MockResponse()
                .setBody("[{\"id\":1}]")
                .addHeader("Content-Type", "application/json"));
        backend.enqueue(new MockResponse()
                .setBody("{\"totalRuns\":3}")
                .addHeader("Content-Type", "application/json"));

        mockMvc.perform(get("/bff/overview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dashboard.totalRuns").value(3));
    }
}
