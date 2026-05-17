package com.workflowbuilder.workflow.service;

import com.workflowbuilder.workflow.api.WorkflowDtos.CreateWorkflowRequest;
import com.workflowbuilder.workflow.api.WorkflowDtos.UpdateWorkflowRequest;
import com.workflowbuilder.workflow.api.WorkflowDtos.WorkflowResponse;
import com.workflowbuilder.workflow.domain.OutboxEvent;
import com.workflowbuilder.workflow.domain.OutboxEventRepository;
import com.workflowbuilder.workflow.domain.WorkflowDefinition;
import com.workflowbuilder.workflow.domain.WorkflowDefinitionRepository;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class WorkflowSchemaService {

    private static final Logger log = LoggerFactory.getLogger(WorkflowSchemaService.class);

    private final WorkflowDefinitionRepository repository;
    private final OutboxEventRepository outboxEventRepository;
    private final StringRedisTemplate redisTemplate;
    private final WorkflowGraphProcessor WorkflowGraphProcessor;

    public WorkflowSchemaService(
            WorkflowDefinitionRepository repository,
            OutboxEventRepository outboxEventRepository,
            StringRedisTemplate redisTemplate,
            WorkflowGraphProcessor WorkflowGraphProcessor
    ) {
        this.repository = repository;
        this.outboxEventRepository = outboxEventRepository;
        this.redisTemplate = redisTemplate;
        this.WorkflowGraphProcessor = WorkflowGraphProcessor;
    }

    @Transactional
    public WorkflowResponse create(CreateWorkflowRequest request) {
        repository.findByName(request.name()).ifPresent(existing -> {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Workflow name already exists");
        });
        WorkflowDefinition definition = new WorkflowDefinition();
        definition.setName(request.name());
        definition.setDefinitionJson(request.definitionJson());
        definition.setStatus("DRAFT");
        definition.setCreatedAt(Instant.now());
        definition.setUpdatedAt(Instant.now());
        WorkflowDefinition saved = repository.save(definition);
        WorkflowGraphProcessor.syncFromDefinitionJson(saved.getId(), saved.getDefinitionJson());
        persistOutbox(saved, "WORKFLOW_CREATED");
        cacheWorkflow(saved);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public List<WorkflowResponse> list() {
        List<WorkflowDefinition> workflows = repository.findAll();
        workflows.forEach(this::cacheWorkflow);
        return workflows.stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public WorkflowResponse get(Long id) {
        return toResponse(getEntity(id));
    }

    @Transactional
    public WorkflowResponse update(Long id, UpdateWorkflowRequest request) {
        WorkflowDefinition definition = getEntity(id);
        definition.setDefinitionJson(request.definitionJson());
        definition.setUpdatedAt(Instant.now());
        WorkflowDefinition saved = repository.save(definition);
        WorkflowGraphProcessor.syncFromDefinitionJson(saved.getId(), saved.getDefinitionJson());
        persistOutbox(saved, "WORKFLOW_UPDATED");
        cacheWorkflow(saved);
        return toResponse(saved);
    }

    @Transactional
    public WorkflowResponse publish(Long id) {
        WorkflowDefinition definition = getEntity(id);
        definition.setStatus("PUBLISHED");
        definition.setUpdatedAt(Instant.now());
        WorkflowDefinition saved = repository.save(definition);
        persistOutbox(saved, "WORKFLOW_PUBLISHED");
        cacheWorkflow(saved);
        return toResponse(saved);
    }

    private WorkflowDefinition getEntity(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Workflow not found"));
    }

    private WorkflowResponse toResponse(WorkflowDefinition definition) {
        return new WorkflowResponse(
                definition.getId(),
                definition.getName(),
                definition.getDefinitionJson(),
                definition.getStatus(),
                definition.getCreatedAt(),
                definition.getUpdatedAt()
        );
    }

    private void persistOutbox(WorkflowDefinition workflow, String eventType) {
        OutboxEvent event = new OutboxEvent();
        event.setAggregateType("workflow");
        event.setAggregateId(workflow.getId());
        event.setEventType(eventType);
        event.setPayload("{\"id\":" + workflow.getId() + ",\"name\":\"" + workflow.getName() + "\",\"status\":\"" + workflow.getStatus() + "\"}");
        event.setStatus("PENDING");
        event.setCreatedAt(Instant.now());
        outboxEventRepository.save(event);
    }

    private void cacheWorkflow(WorkflowDefinition workflow) {
        try {
            redisTemplate.opsForValue().set("workflow:" + workflow.getId(), workflow.getStatus());
        } catch (Exception ex) {
            log.warn("Skipping workflow status cache for id {}: {}", workflow.getId(), ex.getMessage());
        }
    }
}
