package com.workflowbuilder.dlq.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.workflowbuilder.dlq.service.DeadLetterQueueService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class DlqControllerTest {

    @Test
    void replayReturnsCountFromService() {
        DeadLetterQueueService service = Mockito.mock(DeadLetterQueueService.class);
        DeadLetterQueueController controller = new DeadLetterQueueController(service);

        when(service.replayAll()).thenReturn(3);
        when(service.list()).thenReturn(List.of("a", "b"));

        assertEquals(List.of("a", "b"), controller.getFailedMessages());
        assertEquals(Map.of("replayed", 3), controller.replayFailedMessages());
    }
}
