package com.workflowbuilder.workflow.domain;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowNodeRepository extends JpaRepository<WorkflowNode, Long> {

    List<WorkflowNode> findByWorkflow_IdOrderByNodeKey(Long workflowId);

    Optional<WorkflowNode> findByWorkflow_IdAndNodeKey(Long workflowId, String nodeKey);

    boolean existsByWorkflow_IdAndNodeKey(Long workflowId, String nodeKey);

    void deleteByWorkflow_Id(Long workflowId);
}
