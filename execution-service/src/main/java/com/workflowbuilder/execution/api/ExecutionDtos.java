package com.workflowbuilder.execution.api;

import java.time.Instant;

public class ExecutionDtos {

    public record TriggerWorkflowRequest(Long workflowId, String tenantId, String idempotencyKey) {
    }

    public record WorkflowSummary(Long id, String name, String status, String definitionJson) {
    }

    public record WorkflowRunResponse(
            Long id,
            Long workflowId,
            String workflowName,
            String status,
            Instant startedAt,
            Instant finishedAt
    ) {
    }
}
