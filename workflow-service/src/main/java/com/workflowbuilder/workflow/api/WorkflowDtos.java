package com.workflowbuilder.workflow.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public class WorkflowDtos {

    public record CreateWorkflowRequest(
            @NotBlank String name,
            @NotBlank @Size(max = 1_000_000) String definitionJson
    ) {
    }

    public record UpdateWorkflowRequest(
            @NotBlank @Size(max = 1_000_000) String definitionJson
    ) {
    }

    public record WorkflowResponse(
            Long id,
            String name,
            String definitionJson,
            String status,
            Instant createdAt,
            Instant updatedAt
    ) {
    }
}
