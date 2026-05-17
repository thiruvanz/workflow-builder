package com.workflowbuilder.monitoring.service;

import com.workflowbuilder.resilience.OutboundCallGuard;
import com.workflowbuilder.monitoring.api.MonitoringDtos.DashboardResponse;
import com.workflowbuilder.monitoring.api.MonitoringDtos.RunSummary;
import com.workflowbuilder.monitoring.api.MonitoringDtos.WorkflowSummary;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class SystemSystemMonitoringService {

    private final RestClient workflowClient;
    private final RestClient executionClient;
    private final EventMetricsStore eventMetricsStore;
    private final OutboundCallGuard outboundCallGuard;

    public SystemMonitoringService(
            RestClient.Builder builder,
            EventMetricsStore eventMetricsStore,
            OutboundCallGuard outboundCallGuard,
            @Value("${workflow.service.base-url}") String workflowServiceBaseUrl,
            @Value("${execution.service.base-url}") String WorkflowExecutionServiceBaseUrl
    ) {
        this.workflowClient = builder.baseUrl(workflowServiceBaseUrl).build();
        this.executionClient = builder.baseUrl(WorkflowExecutionServiceBaseUrl).build();
        this.eventMetricsStore = eventMetricsStore;
        this.outboundCallGuard = outboundCallGuard;
    }

    public DashboardResponse dashboard() {
        List<WorkflowSummary> workflows = outboundCallGuard.execute(
                "workflowApi",
                () -> workflowClient.get()
                        .uri("/api/workflows")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        }));

        List<RunSummary> runs = outboundCallGuard.execute(
                "executionApi",
                () -> executionClient.get()
                        .uri("/api/executions/runs")
                        .retrieve()
                        .body(new ParameterizedTypeReference<>() {
                        }));

        List<WorkflowSummary> safeWorkflows = workflows == null ? List.of() : workflows;
        List<RunSummary> safeRuns = runs == null ? List.of() : runs;

        Map<String, Long> runsByStatus = safeRuns.stream()
                .collect(Collectors.groupingBy(RunSummary::status, Collectors.counting()));

        int published = (int) safeWorkflows.stream()
                .filter(workflow -> "PUBLISHED".equals(workflow.status()))
                .count();

        return new DashboardResponse(
                safeWorkflows.size(),
                published,
                safeRuns.size(),
                runsByStatus,
                eventMetricsStore.snapshot(),
                safeWorkflows,
                safeRuns
        );
    }
}
