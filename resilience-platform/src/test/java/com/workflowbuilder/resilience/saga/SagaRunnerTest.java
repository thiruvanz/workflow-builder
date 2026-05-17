package com.workflowbuilder.resilience.saga;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

class SagaRunnerTest {

    @Test
    void compensatesWhenMiddleFails() {
        List<String> log = new ArrayList<>();
        SagaRunner runner = new SagaRunner();
        assertThrows(
                IllegalStateException.class,
                () ->
                        runner.run(
                                SagaStep.of(() -> log.add("1"), () -> log.add("-1")),
                                SagaStep.of(
                                        () -> {
                                            throw new IllegalStateException("fail");
                                        },
                                        () -> log.add("-2")),
                                SagaStep.of(() -> log.add("3"), () -> log.add("-3"))));
        assertEquals(List.of("1", "-1"), log);
    }

    @Test
    void runsAllWhenSuccess() throws Exception {
        List<String> log = new ArrayList<>();
        SagaRunner runner = new SagaRunner();
        runner.run(SagaStep.of(() -> log.add("1"), () -> {}), SagaStep.of(() -> log.add("2"), () -> {}));
        assertEquals(List.of("1", "2"), log);
    }

    @Test
    void compensateFailureIsSuppressed() {
        SagaRunner runner = new SagaRunner();
        Exception ex =
                assertThrows(
                        IllegalStateException.class,
                        () ->
                                runner.run(
                                        SagaStep.of(
                                                () -> {},
                                                () -> {
                                                    throw new RuntimeException("comp fail");
                                                }),
                                        SagaStep.of(
                                                () -> {
                                                    throw new IllegalStateException("main");
                                                },
                                                () -> {})));
        assertTrue(ex.getSuppressed().length >= 1);
    }
}
