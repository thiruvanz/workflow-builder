package com.workflowbuilder.workflow.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.workflowbuilder.workflow.api.WorkflowDtos.CreateWorkflowRequest;
import com.workflowbuilder.workflow.api.WorkflowDtos.WorkflowResponse;
import com.workflowbuilder.workflow.service.WorkflowSchemaService;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class WorkflowControllerTest {

    @Test
    void createDelegatesToService() {
        WorkflowSchemaService service = Mockito.mock(WorkflowSchemaService.class);
        WorkflowController controller = new WorkflowController(service);
        CreateWorkflowRequest request = new CreateWorkflowRequest("orders", "{}");
        WorkflowResponse expected = new WorkflowResponse(1L, "orders", "{}", "DRAFT", Instant.now(), Instant.now());

        when(service.create(request)).thenReturn(expected);

        WorkflowResponse actual = controller.create(request);
        assertEquals(expected, actual);
    }

    @Test
    void listDelegatesToService() {
        WorkflowSchemaService service = Mockito.mock(WorkflowSchemaService.class);
        WorkflowController controller = new WorkflowController(service);
        WorkflowResponse row = new WorkflowResponse(1L, "a", "{}", "DRAFT", Instant.now(), Instant.now());
        when(service.list()).thenReturn(java.util.List.of(row));

        assertEquals(1, controller.list().size());
    }

    @Test
    void getDelegatesToService() {
        WorkflowSchemaService service = Mockito.mock(WorkflowSchemaService.class);
        WorkflowController controller = new WorkflowController(service);
        WorkflowResponse row = new WorkflowResponse(9L, "n", "{}", "DRAFT", Instant.now(), Instant.now());
        when(service.get(9L)).thenReturn(row);

        assertEquals(row, controller.get(9L));
    }

    @Test
    void updateDelegatesToService() {
        WorkflowSchemaService service = Mockito.mock(WorkflowSchemaService.class);
        WorkflowController controller = new WorkflowController(service);
        WorkflowDtos.UpdateWorkflowRequest req = new WorkflowDtos.UpdateWorkflowRequest("{}");
        WorkflowResponse row = new WorkflowResponse(2L, "n", "{}", "DRAFT", Instant.now(), Instant.now());
        when(service.update(2L, req)).thenReturn(row);

        assertEquals(row, controller.update(2L, req));
    }

    @Test
    void publishDelegatesToService() {
        WorkflowSchemaService service = Mockito.mock(WorkflowSchemaService.class);
        WorkflowController controller = new WorkflowController(service);
        WorkflowResponse row = new WorkflowResponse(3L, "n", "{}", "PUBLISHED", Instant.now(), Instant.now());
        when(service.publish(3L)).thenReturn(row);

        assertEquals(row, controller.publish(3L));
    }
}
