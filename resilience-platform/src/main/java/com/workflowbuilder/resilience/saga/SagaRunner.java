package com.workflowbuilder.resilience.saga;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs ordered saga steps; on the first failure, runs {@link SagaStep#compensate()} on completed
 * steps in reverse order. Use for multi-step business flows that need undo semantics.
 *
 * <p>For purely <em>read-only</em> aggregation (e.g. BFF GET), circuit breakers alone are enough.
 * For distributed writes, prefer Temporal compensations in {@code execution-service} or
 * choreography over Kafka with explicit compensating events.
 */
public final class SagaRunner {

    public void run(SagaStep first, SagaStep... rest) throws Exception {
        List<SagaStep> steps = new ArrayList<>();
        steps.add(first);
        for (SagaStep s : rest) {
            steps.add(s);
        }
        runAll(steps);
    }

    public void runAll(List<SagaStep> steps) throws Exception {
        List<SagaStep> completed = new ArrayList<>();
        try {
            for (SagaStep step : steps) {
                step.run();
                completed.add(step);
            }
        } catch (Exception ex) {
            for (int i = completed.size() - 1; i >= 0; i--) {
                try {
                    completed.get(i).compensate();
                } catch (Exception suppressed) {
                    ex.addSuppressed(suppressed);
                }
            }
            throw ex;
        }
    }
}
