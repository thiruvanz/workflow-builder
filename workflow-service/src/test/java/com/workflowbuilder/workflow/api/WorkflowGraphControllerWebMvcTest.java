package com.workflowbuilder.workflow.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.CreateEdgeRequest;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.CreateNodeRequest;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.EdgeResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.GraphSummaryResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.NodeResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.UpdateNodeRequest;
import com.workflowbuilder.workflow.service.WorkflowGraphProcessor;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WorkflowGraphApiController.class)
class WorkflowGraphControllerWebMvcTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockBean
    WorkflowGraphProcessor graphService;

    @Test
    void getGraph() throws Exception {
        JsonNode def = objectMapper.readTree("{\"nodes\":[]}");
        when(graphService.getGraph(10L)).thenReturn(new GraphSummaryResponse(10L, def));

        mockMvc.perform(get("/api/workflows/10/graph"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.workflowId").value(10));
    }

    @Test
    void listNodes() throws Exception {
        JsonNode data = objectMapper.readTree("{}");
        when(graphService.listNodes(11L))
                .thenReturn(List.of(new NodeResponse(1L, "n1", "http", 0, 0, data)));

        mockMvc.perform(get("/api/workflows/11/nodes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nodeKey").value("n1"));
    }

    @Test
    void createNode() throws Exception {
        JsonNode data = objectMapper.readTree("{\"url\":\"x\"}");
        CreateNodeRequest req = new CreateNodeRequest("k1", "http", Map.of("x", 1.0, "y", 2.0), data);
        NodeResponse resp = new NodeResponse(2L, "k1", "http", 1, 2, data);
        when(graphService.createNode(eq(12L), any(CreateNodeRequest.class))).thenReturn(resp);

        mockMvc.perform(
                        post("/api/workflows/12/nodes")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(2));
    }

    @Test
    void updateNode() throws Exception {
        JsonNode data = objectMapper.readTree("{}");
        UpdateNodeRequest req = new UpdateNodeRequest(Map.of("x", 0.0, "y", 0.0), data);
        NodeResponse resp = new NodeResponse(3L, "k1", "http", 0, 0, data);
        when(graphService.updateNode(eq(13L), eq("k1"), any(UpdateNodeRequest.class))).thenReturn(resp);

        mockMvc.perform(
                        put("/api/workflows/13/nodes/k1")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteNode() throws Exception {
        mockMvc.perform(delete("/api/workflows/14/nodes/k2")).andExpect(status().isOk());
    }

    @Test
    void listEdges() throws Exception {
        JsonNode payload = objectMapper.readTree("{\"id\":\"e1\"}");
        when(graphService.listEdges(15L)).thenReturn(List.of(new EdgeResponse(1L, "e1", payload)));

        mockMvc.perform(get("/api/workflows/15/edges"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].edgeKey").value("e1"));
    }

    @Test
    void createEdge() throws Exception {
        JsonNode payload = objectMapper.readTree("{\"source\":\"a\"}");
        CreateEdgeRequest req = new CreateEdgeRequest("e2", payload);
        EdgeResponse resp = new EdgeResponse(2L, "e2", payload);
        when(graphService.createEdge(eq(16L), any(CreateEdgeRequest.class))).thenReturn(resp);

        mockMvc.perform(
                        post("/api/workflows/16/edges")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.edgeKey").value("e2"));
    }

    @Test
    void deleteEdge() throws Exception {
        mockMvc.perform(delete("/api/workflows/17/edges/e3")).andExpect(status().isOk());
    }
}
