package com.workflowbuilder.execution.service;

import com.fasterxml.jackson.databind.JsonNode;
import io.temporal.activity.ActivityOptions;
import io.temporal.workflow.Workflow;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.workflowbuilder.execution.workflow.WorkflowGraphPlanner;
import com.workflowbuilder.execution.workflow.WorkflowGraphPlanner.ExecutableStep;

public class WorkflowExecutorImpl implements WorkflowExecutor {

    private static final Logger log = LoggerFactory.getLogger(WorkflowExecutorImpl.class);

    private final WorkflowActivity activity = Workflow.newActivityStub(
            WorkflowActivity.class,
            ActivityOptions.newBuilder()
                    .setStartToCloseTimeout(Duration.ofMinutes(2))
                    .build());

    @Override
    public String execute(Long workflowId, String workflowName, String definitionJson) {
        List<ExecutableStep> steps = WorkflowGraphPlanner.plan(definitionJson);
        log.info("Executing workflow {} ({}) with {} steps", workflowId, workflowName, steps.size());
        for (ExecutableStep step : steps) {
            JsonNode data = step.data();
            switch (step.kind()) {
                case "http" -> {
                    String method = dataText(data, "method", "GET");
                    String url = dataText(data, "url", "");
                    if (!url.isBlank()) {
                        activity.httpRequest(method, url);
                    }
                }
                case "delay" -> {
                    long ms = 1000L;
                    if (data.has("delayMs") && data.get("delayMs").isNumber()) {
                        ms = data.get("delayMs").longValue();
                    }
                    activity.delayMillis(ms);
                }
                case "email" -> activity.sendEmail(
                        dataText(data, "to", ""),
                        dataText(data, "subject", ""),
                        dataText(data, "body", ""));
                case "branch" -> log.debug("Skipping branch node {} (linear engine)", step.nodeId());
                default -> log.debug("Skipping unsupported node type {} id {}", step.kind(), step.nodeId());
            }
        }
        return "completed:" + workflowId + ":" + workflowName;
    }

    private static String dataText(JsonNode data, String field, String defaultValue) {
        if (data == null || !data.has(field) || data.get(field).isNull()) {
            return defaultValue;
        }
        String v = data.get(field).asText("");
        return v.isEmpty() ? defaultValue : v;
    }
}
