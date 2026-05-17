package com.workflowbuilder.bff;

import com.workflowbuilder.resilience.OutboundCallGuard;
import com.workflowbuilder.resilience.saga.SagaRunner;
import com.workflowbuilder.resilience.saga.SagaStep;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

@RestController
@RequestMapping("/bff")
public class BffApiController {
    private final RestClient workflowClient;
    private final RestClient monitoringClient;
    private final OutboundCallGuard outboundCallGuard;
    private final SagaRunner sagaRunner;

    public BffApiController(
            RestClient.Builder builder,
            @Value("${workflow.service.base-url}") String workflowBase,
            @Value("${monitoring.service.base-url}") String monitoringBase,
            OutboundCallGuard outboundCallGuard,
            SagaRunner sagaRunner) {
        this.workflowClient = builder.baseUrl(workflowBase).build();
        this.monitoringClient = builder.baseUrl(monitoringBase).build();
        this.outboundCallGuard = outboundCallGuard;
        this.sagaRunner = sagaRunner;
    }

    
    @GetMapping("/finaloverview")
    public Map<String, Object> getConsolidatedOverview() throws Exception {
        Object[] appworkflow = new Object[1];
        Object[] dashboard = new Object[1];
        sagaRunner.runAll(List.of(
                SagaStep.of(
                        () -> appworkflow[0] = outboundCallGuard.execute(
                                "workflowApi",
                                () -> workflowClient.get()
                                        .uri("/api/workflows")
                                        .retrieve()
                                        .body(Object.class)),
                        () -> {
                        }),
                SagaStep.of(
                        () -> dashboard[0] = outboundCallGuard.execute(
                                "monitoringApi",
                                () -> monitoringClient.get()
                                        .uri("/api/monitoring/dashboard")
                                        .retrieve()
                                        .body(Object.class)),
                        () -> {
                        })));
        return Map.of("workflows", appworkflow[0], "dashboard", dashboard[0]);
    }
}
