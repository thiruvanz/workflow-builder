package com.workflowbuilder.frontend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class MicroserviceClientFactory {

    private final RestClient workflowClient;
    private final RestClient executionClient;
    private final RestClient monitoringClient;

    public MicroserviceClientFactory(
            @Value("${workflow.service.base-url}") String workflowBaseUrl,
            @Value("${execution.service.base-url}") String executionBaseUrl,
            @Value("${monitoring.service.base-url}") String monitoringBaseUrl) {
        // Use a plain builder: the auto-configured RestClient.Builder is load-balanced for Eureka,
        // which breaks literal hosts like http://localhost:8081 from a local dev machine.
        this.workflowClient = RestClient.builder().baseUrl(workflowBaseUrl).build();
        this.executionClient = RestClient.builder().baseUrl(executionBaseUrl).build();
        this.monitoringClient = RestClient.builder().baseUrl(monitoringBaseUrl).build();
    }

    public RestClient workflowClient() {
        return workflowClient;
    }

    public RestClient executionClient() {
        return executionClient;
    }

    public RestClient monitoringClient() {
        return monitoringClient;
    }
}
