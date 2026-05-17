package com.workflowbuilder.trigger.security;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

/**
 * When {@code trigger.webhook.secret} is non-empty, POST /api/triggers/webhook must send
 * {@code X-Webhook-Secret} with a matching value (compared in constant time for equal-length secrets).
 */
@Component
public class WebhookSecretValidator {

    private final byte[] configuredSecretBytes;

    public WebhookSecretValidator(@Value("${trigger.webhook.secret:}") String configuredSecret) {
        String trimmed = configuredSecret == null ? "" : configuredSecret.trim();
        this.configuredSecretBytes = trimmed.isEmpty() ? null : trimmed.getBytes(StandardCharsets.UTF_8);
    }

    /**
     * @param presentedSecret value of the {@code X-Webhook-Secret} header (may be null if absent)
     */
    public void validate(String presentedSecret) {
        if (configuredSecretBytes == null) {
            return;
        }
        if (presentedSecret == null || presentedSecret.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing X-Webhook-Secret header");
        }
        byte[] presented = presentedSecret.getBytes(StandardCharsets.UTF_8);
        if (presented.length != configuredSecretBytes.length
                || !MessageDigest.isEqual(configuredSecretBytes, presented)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid webhook secret");
        }
    }
}
