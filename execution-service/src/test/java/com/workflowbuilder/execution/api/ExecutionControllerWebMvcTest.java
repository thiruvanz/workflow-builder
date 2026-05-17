package com.workflowbuilder.execution.api;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowbuilder.execution.api.ExecutionDtos.TriggerWorkflowRequest;
import com.workflowbuilder.execution.api.ExecutionDtos.WorkflowRunResponse;
import com.workflowbuilder.execution.service.WorkflowExecutionService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkflowExecutionController.class)
class ExecutionControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    WorkflowExecutionService service;

    @Test
    void postTriggerReturnsRun() throws Exception {
        WorkflowRunResponse body =
                new WorkflowRunResponse(1L, 10L, "wf", "RUNNING", Instant.parse("2020-01-01T00:00:00Z"), null);
        when(service.trigger(eq(10L), eq("t1"), eq("id1"))).thenReturn(body);

        mockMvc.perform(
                        post("/api/executions/trigger")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new TriggerWorkflowRequest(10L, "t1", "id1"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.workflowId").value(10));
    }

    @Test
    void getRunsReturnsList() throws Exception {
        WorkflowRunResponse row =
                new WorkflowRunResponse(2L, 11L, "wf", "COMPLETED", Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2020-01-02T00:00:00Z"));
        when(service.listRuns()).thenReturn(List.of(row));

        mockMvc.perform(get("/api/executions/runs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("COMPLETED"));
    }
}
