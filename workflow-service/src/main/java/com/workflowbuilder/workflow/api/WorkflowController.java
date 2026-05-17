package com.workflowbuilder.workflow.api;

import com.workflowbuilder.workflow.api.WorkflowDtos.CreateWorkflowRequest;
import com.workflowbuilder.workflow.api.WorkflowDtos.UpdateWorkflowRequest;
import com.workflowbuilder.workflow.api.WorkflowDtos.WorkflowResponse;
import com.workflowbuilder.workflow.service.WorkflowSchemaService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/workflows")
public class WorkflowApiController {

    private final WorkflowSchemaService service;

    public WorkflowApiController(WorkflowSchemaService service) {
        this.service = service;
    }

    @PostMapping
    public WorkflowResponse create(@Valid @RequestBody CreateWorkflowRequest request) {
        return service.create(request);
    }

    @GetMapping
    public List<WorkflowResponse> list() {
        return service.list();
    }

    @GetMapping("/{id}")
    public WorkflowResponse get(@PathVariable Long id) {
        return service.get(id);
    }

    @PutMapping("/{id}")
    public WorkflowResponse update(@PathVariable Long id, @Valid @RequestBody UpdateWorkflowRequest request) {
        return service.update(id, request);
    }

    @PostMapping("/{id}/publish")
    public WorkflowResponse publish(@PathVariable Long id) {
        return service.publish(id);
    }
}
