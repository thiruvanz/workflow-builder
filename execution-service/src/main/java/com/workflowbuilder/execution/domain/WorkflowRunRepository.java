package com.workflowbuilder.execution.domain;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WorkflowRunRepository extends JpaRepository<WorkflowRun, Long> {
    Optional<WorkflowRun> findByIdempotencyKey(String idempotencyKey);
}
