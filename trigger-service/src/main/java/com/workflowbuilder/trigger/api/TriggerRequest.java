package com.workflowbuilder.trigger.api;

public record TriggerRequest(Long workflowId, String tenantId, String idempotencyKey) {
}
