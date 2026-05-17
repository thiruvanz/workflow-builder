package com.workflowbuilder.execution.api;

import com.workflowbuilder.execution.api.ExecutionDtos.TriggerWorkflowRequest;
import com.workflowbuilder.execution.api.ExecutionDtos.WorkflowRunResponse;
import com.workflowbuilder.execution.service.WorkflowExecutionService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/executions")
public class WorkflowExecutionController {

    private final WorkflowExecutionService service;

    public WorkflowExecutionController(WorkflowExecutionService service) {
        this.service = service;
    }

    @PostMapping("/trigger")
    public WorkflowRunResponse trigger(@RequestBody TriggerWorkflowRequest request) {
        return service.trigger(request.workflowId(), request.tenantId(), request.idempotencyKey());
    }

    @GetMapping("/runs")
    public List<WorkflowRunResponse> runs() {
        return service.listRuns();
    }
}
