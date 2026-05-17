package com.workflowbuilder.workflow.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowEdgeRepository extends JpaRepository<WorkflowEdge, Long> {

    List<WorkflowEdge> findByWorkflow_IdOrderByEdgeKey(Long workflowId);

    Optional<WorkflowEdge> findByWorkflow_IdAndEdgeKey(Long workflowId, String edgeKey);

    boolean existsByWorkflow_IdAndEdgeKey(Long workflowId, String edgeKey);

    void deleteByWorkflow_Id(Long workflowId);
}
