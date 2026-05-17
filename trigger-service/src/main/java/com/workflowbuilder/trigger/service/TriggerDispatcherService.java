package com.workflowbuilder.trigger.service;

import com.workflowbuilder.resilience.OutboundCallGuard;
import com.workflowbuilder.trigger.api.TriggerRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class WebhookDispatcherService {

    private static final Logger log = LoggerFactory.getLogger(WebhookDispatcherService.class);

    private final RestClient executionRestClient;
    private final OutboundCallGuard outboundCallGuard;
    private final long cronWorkflowId;

    public WebhookDispatcherService(
            @Value("${execution.service.base-url}") String executionBaseUrl,
            @Value("${trigger.cron.workflow-id}") long cronWorkflowId,
            OutboundCallGuard outboundCallGuard) {
        String base = stripTrailingSlash(executionBaseUrl);
        this.executionRestClient = RestClient.builder().baseUrl(base).build();
        this.cronWorkflowId = cronWorkflowId;
        this.outboundCallGuard = outboundCallGuard;
    }

    private static String stripTrailingSlash(String url) {
        if (url == null || url.isBlank()) {
            return url;
        }
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public void dispatch(TriggerRequest request) {
        try {
            outboundCallGuard.executeVoid(
                    "executionApi",
                    () -> executionRestClient
                            .post()
                            .uri("/api/executions/trigger")
                            .body(request)
                            .retrieve()
                            .toBodilessEntity());
        } catch (Exception e) {
            log.warn("Failed to dispatch trigger to execution-service: {}", e.getMessage());
            throw new RuntimeException("Failed to dispatch trigger", e);
        }
    }

    @Scheduled(cron = "${trigger.cron.expression}")
    public void dispatchCron() {
        dispatch(new TriggerRequest(cronWorkflowId, "system-cron", "cron-" + System.currentTimeMillis()));
    }
}
