package com.workflowbuilder.frontend.api;

import com.workflowbuilder.frontend.service.MicroserviceClientFactory;
import com.workflowbuilder.resilience.OutboundCallGuard;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/ui-api")
public class FrontendGatewayController {

    private final MicroserviceClientFactory MicroserviceClientFactory;
    private final OutboundCallGuard outboundCallGuard;

    public FrontendGatewayController(MicroserviceClientFactory MicroserviceClientFactory, OutboundCallGuard outboundCallGuard) {
        this.MicroserviceClientFactory = MicroserviceClientFactory;
        this.outboundCallGuard = outboundCallGuard;
    }

    private <T> T callWorkflowService(Supplier<T> supplier) {
        return outboundCallGuard.execute("workflowApi", supplier);
    }

    private <T> T callWorkflowExecutionService(Supplier<T> supplier) {
        return outboundCallGuard.execute("executionApi", supplier);
    }

    private <T> T callSystemMonitoringService(Supplier<T> supplier) {
        return outboundCallGuard.execute("monitoringApi", supplier);
    }

    private void callWorkflowServiceVoid(Runnable runnable) {
        outboundCallGuard.executeVoid("workflowApi", runnable);
    }

    @GetMapping("/workflows")
    public List<Map<String, Object>> listWorkflows() {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().get()
                .uri("/api/workflows")
                .retrieve()
                .body(new ParameterizedTypeReference<List<Map<String, Object>>>() {
                }));
    }

    @PostMapping("/workflows")
    public Object createWorkflow(@RequestBody Map<String, Object> workflowRequest) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().post()
                .uri("/api/workflows")
                .body(workflowRequest)
                .retrieve()
                .body(Object.class));
    }

    @PutMapping("/workflows/{id}")
    public Object updateWorkflow(@PathVariable Long id, @RequestBody Map<String, Object> workflowRequest) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().put()
                .uri("/api/workflows/{id}", id)
                .body(workflowRequest)
                .retrieve()
                .body(Object.class));
    }

    @PostMapping("/workflows/{id}/publish")
    public Object publishWorkflow(@PathVariable Long id) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().post()
                .uri("/api/workflows/{id}/publish", id)
                .retrieve()
                .body(Object.class));
    }

    @GetMapping("/workflows/{id}/graph")
    public Object getWorkflowGraph(@PathVariable Long id) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().get()
                .uri("/api/workflows/{id}/graph", id)
                .retrieve()
                .body(Object.class));
    }

    @GetMapping("/workflows/{id}/nodes")
    public Object listWorkflowNodes(@PathVariable Long id) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().get()
                .uri("/api/workflows/{id}/nodes", id)
                .retrieve()
                .body(Object.class));
    }

    @PostMapping("/workflows/{id}/nodes")
    public Object createWorkflowNode(@PathVariable Long id, @RequestBody Map<String, Object> nodeRequest) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().post()
                .uri("/api/workflows/{id}/nodes", id)
                .body(nodeRequest)
                .retrieve()
                .body(Object.class));
    }

    @PutMapping("/workflows/{id}/nodes/{nodeKey}")
    public Object updateWorkflowNode(
            @PathVariable Long id,
            @PathVariable String nodeKey,
            @RequestBody Map<String, Object> nodeRequest
    ) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().put()
                .uri("/api/workflows/{id}/nodes/{nodeKey}", id, nodeKey)
                .body(nodeRequest)
                .retrieve()
                .body(Object.class));
    }

    @DeleteMapping("/workflows/{id}/nodes/{nodeKey}")
    public void deleteWorkflowNode(@PathVariable Long id, @PathVariable String nodeKey) {
        callWorkflowServiceVoid(() -> MicroserviceClientFactory.workflowClient().delete()
                .uri("/api/workflows/{id}/nodes/{nodeKey}", id, nodeKey)
                .retrieve()
                .toBodilessEntity());
    }

    @GetMapping("/workflows/{id}/edges")
    public Object listWorkflowEdges(@PathVariable Long id) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().get()
                .uri("/api/workflows/{id}/edges", id)
                .retrieve()
                .body(Object.class));
    }

    @PostMapping("/workflows/{id}/edges")
    public Object createWorkflowEdge(@PathVariable Long id, @RequestBody Map<String, Object> edgeRequest) {
        return callWorkflowService(() -> MicroserviceClientFactory.workflowClient().post()
                .uri("/api/workflows/{id}/edges", id)
                .body(edgeRequest)
                .retrieve()
                .body(Object.class));
    }

    @DeleteMapping("/workflows/{id}/edges/{edgeKey}")
    public void deleteWorkflowEdge(@PathVariable Long id, @PathVariable String edgeKey) {
        callWorkflowServiceVoid(() -> MicroserviceClientFactory.workflowClient().delete()
                .uri("/api/workflows/{id}/edges/{edgeKey}", id, edgeKey)
                .retrieve()
                .toBodilessEntity());
    }

    @PostMapping("/executions/trigger")
    public Object triggerWorkflow(@RequestBody Map<String, Object> triggerRequest) {
        return callWorkflowExecutionService(() -> MicroserviceClientFactory.executionClient().post()
                .uri("/api/executions/trigger")
                .body(triggerRequest)
                .retrieve()
                .body(Object.class));
    }

    @GetMapping("/monitoring/dashboard")
    public Object getMonitoringDashboard() {
        return callSystemMonitoringService(() -> MicroserviceClientFactory.monitoringClient().get()
                .uri("/api/monitoring/dashboard")
                .retrieve()
                .body(Object.class));
    }
}
