package com.workflowbuilder.monitoring.api;

import java.util.List;
import java.util.Map;

public class MonitoringDtos {

    public record WorkflowSummary(Long id, String name, String status) {
    }

    public record RunSummary(Long id, Long workflowId, String workflowName, String status) {
    }

    public record DashboardResponse(
            int totalWorkflows,
            int publishedWorkflows,
            int totalRuns,
            Map<String, Long> runsByStatus,
            Map<String, Long> eventCounters,
            List<WorkflowSummary> workflows,
            List<RunSummary> runs
    ) {
    }
}
