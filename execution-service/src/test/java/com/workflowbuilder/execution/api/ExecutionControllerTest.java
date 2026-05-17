package com.workflowbuilder.execution.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.workflowbuilder.execution.api.ExecutionDtos.TriggerWorkflowRequest;
import com.workflowbuilder.execution.api.ExecutionDtos.WorkflowRunResponse;
import com.workflowbuilder.execution.service.WorkflowExecutionService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ExecutionControllerTest {

    @Test
    void triggerDelegatesToService() {
        WorkflowExecutionService service = Mockito.mock(WorkflowExecutionService.class);
        ExecutionController controller = new ExecutionController(service);
        TriggerWorkflowRequest request = new TriggerWorkflowRequest(10L, "tenant-a", "idem-1");
        WorkflowRunResponse expected = new WorkflowRunResponse(5L, 10L, "wf", "COMPLETED", Instant.now(), Instant.now());

        when(service.trigger(10L, "tenant-a", "idem-1")).thenReturn(expected);

        WorkflowRunResponse actual = controller.trigger(request);
        assertEquals(expected, actual);
    }

    @Test
    void runsDelegatesToService() {
        WorkflowExecutionService service = Mockito.mock(WorkflowExecutionService.class);
        ExecutionController controller = new ExecutionController(service);
        WorkflowRunResponse row =
                new WorkflowRunResponse(1L, 2L, "n", "RUNNING", Instant.now(), null);
        when(service.listRuns()).thenReturn(java.util.List.of(row));

        assertEquals(1, controller.runs().size());
    }
}
