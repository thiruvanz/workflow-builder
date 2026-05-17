package com.workflowbuilder.workflow.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowbuilder.workflow.api.WorkflowDtos.CreateWorkflowRequest;
import com.workflowbuilder.workflow.api.WorkflowDtos.UpdateWorkflowRequest;
import com.workflowbuilder.workflow.api.WorkflowDtos.WorkflowResponse;
import com.workflowbuilder.workflow.service.WorkflowSchemaService;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkflowApiController.class)
class WorkflowControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    WorkflowSchemaService service;

    @Test
    void postCreateReturnsJson() throws Exception {
        WorkflowResponse body = new WorkflowResponse(
                1L, "wf", "{}", "DRAFT", Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2020-01-01T00:00:00Z"));
        when(service.create(any(CreateWorkflowRequest.class))).thenReturn(body);

        mockMvc.perform(
                        post("/api/workflows")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new CreateWorkflowRequest("wf", "{}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("wf"));
    }

    @Test
    void getListReturnsArray() throws Exception {
        WorkflowResponse row = new WorkflowResponse(
                2L, "a", "{}", "DRAFT", Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2020-01-01T00:00:00Z"));
        when(service.list()).thenReturn(List.of(row));

        mockMvc.perform(get("/api/workflows"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(2));
    }

    @Test
    void getByIdReturnsWorkflow() throws Exception {
        WorkflowResponse row = new WorkflowResponse(
                3L, "b", "{}", "PUBLISHED", Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2020-01-01T00:00:00Z"));
        when(service.get(3L)).thenReturn(row);

        mockMvc.perform(get("/api/workflows/3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void putUpdateReturnsJson() throws Exception {
        WorkflowResponse row = new WorkflowResponse(
                4L, "c", "{\"v\":2}", "DRAFT", Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2020-01-02T00:00:00Z"));
        when(service.update(eq(4L), any(UpdateWorkflowRequest.class))).thenReturn(row);

        mockMvc.perform(
                        put("/api/workflows/4")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(new UpdateWorkflowRequest("{\"v\":2}"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.definitionJson").value("{\"v\":2}"));
    }

    @Test
    void postPublishReturnsJson() throws Exception {
        WorkflowResponse row = new WorkflowResponse(
                5L, "d", "{}", "PUBLISHED", Instant.parse("2020-01-01T00:00:00Z"), Instant.parse("2020-01-03T00:00:00Z"));
        when(service.publish(5L)).thenReturn(row);

        mockMvc.perform(post("/api/workflows/5/publish"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }
}
