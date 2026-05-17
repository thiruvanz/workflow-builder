package com.workflowbuilder.trigger.security;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

class WebhookSecretValidatorTest {

    @Test
    void emptyConfiguredSecret_skipsValidation() {
        WebhookSecretValidator validator = new WebhookSecretValidator("");
        assertDoesNotThrow(() -> validator.validate(null));
        assertDoesNotThrow(() -> validator.validate("anything"));
    }

    @Test
    void configuredSecret_requiresMatchingHeader() {
        WebhookSecretValidator validator = new WebhookSecretValidator("expected-secret");
        assertDoesNotThrow(() -> validator.validate("expected-secret"));
    }

    @Test
    void configuredSecret_rejectsMissingHeader() {
        WebhookSecretValidator validator = new WebhookSecretValidator("expected-secret");
        assertThrows(ResponseStatusException.class, () -> validator.validate(null));
    }

    @Test
    void configuredSecret_rejectsWrongHeader() {
        WebhookSecretValidator validator = new WebhookSecretValidator("expected-secret");
        assertThrows(ResponseStatusException.class, () -> validator.validate("wrong"));
    }
}
