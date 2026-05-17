package com.workflowbuilder.resilience.saga;

/**
 * One forward action and its compensating action for a saga (e.g. reserve then release).
 */
@FunctionalInterface
public interface SagaStep {

    void run() throws Exception;

    default void compensate() {
        // no-op by default
    }

    static SagaStep of(ThrowingRunnable run, Runnable compensate) {
        return new SagaStep() {
            @Override
            public void run() throws Exception {
                run.run();
            }

            @Override
            public void compensate() {
                compensate.run();
            }
        };
    }

    @FunctionalInterface
    interface ThrowingRunnable {
        void run() throws Exception;
    }
}
