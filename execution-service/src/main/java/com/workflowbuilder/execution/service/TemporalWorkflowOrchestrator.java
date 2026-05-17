package com.workflowbuilder.execution.service;

import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class TemporalWorkflowOrchestrator {
    private static final Logger log = LoggerFactory.getLogger(TemporalWorkflowOrchestrator.class);
    private static final String TASK_QUEUE = "workflow-execution-task-queue";
    private final WorkflowClient workflowClient;
    private final WorkflowActivityImpl activities;
    private WorkerFactory workerFactory;
    private final AtomicBoolean workerStarted = new AtomicBoolean(false);
    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();

    public TemporalWorkflowOrchestrator(WorkflowClient workflowClient, WorkflowActivityImpl activities) {
        this.workflowClient = workflowClient;
        this.activities = activities;
    }

    @PostConstruct
    public void startWorker() {
        scheduleWorkerStart(0);
    }

    private void scheduleWorkerStart(long delaySeconds) {
        scheduler.schedule(() -> {
            try {
                this.workerFactory = WorkerFactory.newInstance(workflowClient);
                Worker worker = workerFactory.newWorker(TASK_QUEUE);
                worker.registerWorkflowImplementationTypes(WorkflowExecutorImpl.class);
                worker.registerActivitiesImplementations(activities);
                workerFactory.start();
                workerStarted.set(true);
                log.info("Temporal worker started successfully");
                scheduler.shutdown();
            } catch (Exception e) {
                log.warn("Temporal worker failed to start, retrying in 10s: {}", e.getMessage());
                scheduleWorkerStart(10);
            }
        }, delaySeconds, TimeUnit.SECONDS);
    }

    public String startWorkflow(Long workflowId, String workflowName, String definitionJson) {
        WorkflowExecutor workflow = workflowClient.newWorkflowStub(
                WorkflowExecutor.class,
                WorkflowOptions.newBuilder().setTaskQueue(TASK_QUEUE).build()
        );
        return workflow.execute(workflowId, workflowName, definitionJson);
    }

    @PreDestroy
    public void stop() {
        scheduler.shutdownNow();
        if (workerFactory != null) {
            workerFactory.shutdown();
        }
    }
}
