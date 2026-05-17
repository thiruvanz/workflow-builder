package com.workflowbuilder.notify.api;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import com.workflowbuilder.notify.service.NotificationSink;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class NotificationControllerTest {

    @Test
    void messagesReturnsLatestFeed() {
        NotificationSink sink = Mockito.mock(NotificationSink.class);
        NotificationApiController controller = new NotificationApiController(sink);
        when(sink.latest()).thenReturn(List.of("m1", "m2"));

        assertEquals(List.of("m1", "m2"), controller.getLatestNotifications());
    }
}
