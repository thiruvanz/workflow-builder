package com.workflowbuilder.execution.service;

import com.workflowbuilder.kafka.KafkaPartitionKeys;
import com.workflowbuilder.kafka.config.KafkaTopicsProperties;
import com.workflowbuilder.resilience.OutboundCallGuard;
import com.workflowbuilder.execution.api.ExecutionDtos.WorkflowRunResponse;
import com.workflowbuilder.execution.api.ExecutionDtos.WorkflowSummary;
import com.workflowbuilder.execution.domain.WorkflowRun;
import com.workflowbuilder.execution.domain.WorkflowRunRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkflowWorkflowExecutionService {

    private final WorkflowRunRepository repository;
    private final RestClient restClient;
    private final OutboundCallGuard outboundCallGuard;
    private final StringRedisTemplate redisTemplate;
    private final TemporalWorkflowOrchestrator temporalWorkflowOrchestrator;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final KafkaTopicsProperties topicNames;

    public WorkflowExecutionService(
            WorkflowRunRepository repository,
            StringRedisTemplate redisTemplate,
            TemporalWorkflowOrchestrator temporalWorkflowOrchestrator,
            KafkaTemplate<String, String> kafkaTemplate,
            KafkaTopicsProperties topicNames,
            OutboundCallGuard outboundCallGuard,
            @Value("${workflow.service.base-url}") String workflowServiceBaseUrl
    ) {
        this.repository = repository;
        this.restClient = RestClient.builder().baseUrl(workflowServiceBaseUrl).build();
        this.outboundCallGuard = outboundCallGuard;
        this.redisTemplate = redisTemplate;
        this.temporalWorkflowOrchestrator = temporalWorkflowOrchestrator;
        this.kafkaTemplate = kafkaTemplate;
        this.topicNames = topicNames;
    }

    public WorkflowRunResponse trigger(Long workflowId, String tenantId, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            WorkflowRun existing = repository.findByIdempotencyKey(idempotencyKey).orElse(null);
            if (existing != null) {
                return toResponse(existing);
            }
        }
        enforceRateLimit(tenantId == null ? "default" : tenantId);

        WorkflowSummary workflow = outboundCallGuard.execute(
                "workflowApi",
                () -> restClient.get()
                        .uri("/api/workflows/{id}", workflowId)
                        .retrieve()
                        .onStatus(statusCode -> statusCode.value() == 404, (request, response) -> {
                            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found");
                        })
                        .body(WorkflowSummary.class));

        if (workflow == null || !"PUBLISHED".equals(workflow.status())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Workflow must be published before trigger");
        }

        WorkflowRun run = new WorkflowRun();
        run.setWorkflowId(workflow.id());
        run.setWorkflowName(workflow.name());
        run.setTenantId(tenantId);
        run.setIdempotencyKey(idempotencyKey);
        run.setStartedAt(Instant.now());
        run.setStatus("RUNNING");
        WorkflowRun saved = repository.save(run);

        try {
            temporalWorkflowOrchestrator.startWorkflow(
                    workflow.id(),
                    workflow.name(),
                    workflow.definitionJson() != null ? workflow.definitionJson() : "{}");
            saved.setStatus("COMPLETED");
            saved.setFinishedAt(Instant.now().plusSeconds(2));
            WorkflowRun persisted = repository.save(saved);
            kafkaTemplate.send(
                    topicNames.executionEventsTopic(),
                    KafkaPartitionKeys.forWorkflowId(workflow.id()),
                    "{\"workflowId\":" + workflow.id() + ",\"workflowName\":\"" + workflow.name() + "\",\"status\":\"COMPLETED\"}");
            return toResponse(persisted);
        } catch (Exception ex) {
            saved.setStatus("FAILED");
            saved.setFinishedAt(Instant.now());
            WorkflowRun failed = repository.save(saved);
            kafkaTemplate.send(
                    topicNames.executionDlqTopic(),
                    KafkaPartitionKeys.forWorkflowId(workflow.id()),
                    "{\"workflowId\":" + workflow.id() + ",\"workflowName\":\"" + workflow.name() + "\",\"status\":\"FAILED\",\"reason\":\"" + ex.getMessage() + "\"}");
            return toResponse(failed);
        }
    }

    public List<WorkflowRunResponse> listRuns() {
        return repository.findAll().stream().map(this::toResponse).toList();
    }

    private WorkflowRunResponse toResponse(WorkflowRun run) {
        return new WorkflowRunResponse(
                run.getId(),
                run.getWorkflowId(),
                run.getWorkflowName(),
                run.getStatus(),
                run.getStartedAt(),
                run.getFinishedAt()
        );
    }

    private void enforceRateLimit(String tenantId) {
        String key = "rate-limit:" + tenantId;
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, java.time.Duration.ofSeconds(60));
        }
        if (count != null && count > 20) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Tenant rate limit exceeded");
        }
    }
}
