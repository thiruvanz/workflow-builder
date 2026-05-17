package com.workflowbuilder.workflow.api;

import com.workflowbuilder.workflow.api.WorkflowGraphDtos.CreateEdgeRequest;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.CreateNodeRequest;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.EdgeResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.GraphSummaryResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.NodeResponse;
import com.workflowbuilder.workflow.api.WorkflowGraphDtos.UpdateNodeRequest;
import com.workflowbuilder.workflow.service.WorkflowGraphProcessor;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows/{workflowId}")
public class WorkflowGraphApiController {

    private final WorkflowGraphProcessor graphService;

    public WorkflowGraphApiController(WorkflowGraphProcessor service) {
        this.graphService = graphService;
    }

    @GetMapping("/graph")
    public GraphSummaryResponse getGraph(@PathVariable Long workflowId) {
        return graphService.getGraph(workflowId);
    }

    @GetMapping("/nodes")
    public List<NodeResponse> listNodes(@PathVariable Long workflowId) {
        return graphService.listNodes(workflowId);
    }

    @PostMapping("/nodes")
    public NodeResponse createNode(
            @PathVariable Long workflowId,
            @Valid @RequestBody CreateNodeRequest request
    ) {
        return graphService.createNode(workflowId, request);
    }

    @PutMapping("/nodes/{nodeKey}")
    public NodeResponse updateNode(
            @PathVariable Long workflowId,
            @PathVariable String nodeKey,
            @Valid @RequestBody UpdateNodeRequest request
    ) {
        return graphService.updateNode(workflowId, nodeKey, request);
    }

    @DeleteMapping("/nodes/{nodeKey}")
    public void deleteNode(@PathVariable Long workflowId, @PathVariable String nodeKey) {
        graphService.deleteNode(workflowId, nodeKey);
    }

    @GetMapping("/edges")
    public List<EdgeResponse> listEdges(@PathVariable Long workflowId) {
        return graphService.listEdges(workflowId);
    }

    @PostMapping("/edges")
    public EdgeResponse createEdge(
            @PathVariable Long workflowId,
            @Valid @RequestBody CreateEdgeRequest request
    ) {
        return graphService.createEdge(workflowId, request);
    }

    @DeleteMapping("/edges/{edgeKey}")
    public void deleteEdge(@PathVariable Long workflowId, @PathVariable String edgeKey) {
        graphService.deleteEdge(workflowId, edgeKey);
    }
}
