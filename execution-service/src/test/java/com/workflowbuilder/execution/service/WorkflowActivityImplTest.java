package com.workflowbuilder.execution.service;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.workflowbuilder.resilience.OutboundCallGuard;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;

@ExtendWith(MockitoExtension.class)
class WorkflowActivityImplTest {

    @Mock
    JavaMailSender mailSender;

    @Mock
    MimeMessage mimeMessage;

    private static OutboundCallGuard testGuard() {
        return new OutboundCallGuard(CircuitBreakerRegistry.ofDefaults());
    }

    @Test
    void sendEmail_withoutJavaMailSender_doesNotThrow() {
        WorkflowActivityImpl activities = new WorkflowActivityImpl(null, "noreply@test", testGuard());
        activities.sendEmail("a@example.com", "sub", "body");
    }

    @Test
    void sendEmail_withJavaMailSender_sendsMessage() throws Exception {
        when(mailSender.createMimeMessage()).thenReturn(mimeMessage);
        WorkflowActivityImpl activities = new WorkflowActivityImpl(mailSender, "noreply@test", testGuard());
        activities.sendEmail("a@example.com", "Subject", "Hello");
        verify(mailSender).send(mimeMessage);
    }

    @Test
    void sendEmail_blankRecipient_doesNotSend() {
        WorkflowActivityImpl activities = new WorkflowActivityImpl(mailSender, "noreply@test", testGuard());
        activities.sendEmail("   ", "Subject", "Hello");
        verifyNoInteractions(mailSender);
    }
}
