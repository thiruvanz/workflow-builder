package com.workflowbuilder.execution.service;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface WorkflowExecutor {

    @WorkflowMethod
    String execute(Long workflowId, String workflowName, String definitionJson);
}
