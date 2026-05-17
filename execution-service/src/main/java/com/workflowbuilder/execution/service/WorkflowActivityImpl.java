package com.workflowbuilder.execution.service;

import com.workflowbuilder.resilience.OutboundCallGuard;
import java.net.URI;
import java.util.Locale;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class WorkflowActivityImpl implements WorkflowActivity {

    private static final Logger log = LoggerFactory.getLogger(WorkflowActivityImpl.class);

    private final Optional<JavaMailSender> mailSender;
    private final String mailFrom;
    private final OutboundCallGuard outboundCallGuard;
    private final RestClient httpClient = RestClient.builder().build();

    public WorkflowActivityImpl(
            @Autowired(required = false) JavaMailSender mailSender,
            @Value("${spring.mail.from}") String mailFrom,
            OutboundCallGuard outboundCallGuard) {
        this.mailSender = Optional.ofNullable(mailSender);
        this.mailFrom = mailFrom;
        this.outboundCallGuard = outboundCallGuard;
    }

    @Override
    public void delayMillis(long millis) {
        long capped = Math.min(Math.max(millis, 0L), 120_000L);
        try {
            Thread.sleep(capped);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("delay interrupted", e);
        }
    }

    @Override
    public String httpRequest(String method, String url) {
        if (url == null || url.isBlank()) {
            return "skipped:empty-url";
        }
        String u = url.trim();
        if (!u.startsWith("http://") && !u.startsWith("https://")) {
            throw new IllegalArgumentException("Only http(s) URLs are allowed");
        }
        HttpMethod httpMethod = parseMethod(method);
        try {
            return outboundCallGuard.execute(
                    "httpActivity",
                    () -> httpClient
                            .method(httpMethod)
                            .uri(URI.create(u))
                            .retrieve()
                            .toBodilessEntity()
                            .getStatusCode()
                            .toString());
        } catch (RestClientException ex) {
            log.warn("HTTP {} {} failed: {}", httpMethod, u, ex.getMessage());
            throw ex;
        }
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        if (to == null || to.isBlank()) {
            log.warn("email skipped: empty recipient");
            return;
        }
        if (mailSender.isEmpty()) {
            log.info(
                    "EMAIL (not sent — set spring.mail.host and credentials): to={} subject={}",
                    to,
                    subject);
            return;
        }
        try {
            var message = mailSender.get().createMimeMessage();
            var helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(mailFrom);
            helper.setTo(to.trim().split("\\s*,\\s*"));
            helper.setSubject(subject != null ? subject : "");
            helper.setText(body != null ? body : "", false);
            mailSender.get().send(message);
            log.info("Email sent to {}", to);
        } catch (Exception ex) {
            log.error("sendEmail failed: {}", ex.getMessage());
            throw new RuntimeException("Failed to send email", ex);
        }
    }

    private static HttpMethod parseMethod(String method) {
        if (method == null || method.isBlank()) {
            return HttpMethod.GET;
        }
        try {
            return HttpMethod.valueOf(method.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return HttpMethod.GET;
        }
    }
}
