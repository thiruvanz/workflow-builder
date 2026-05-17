package com.workflowbuilder.execution.service;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;

@ActivityInterface
public interface WorkflowActivity {

    @ActivityMethod
    void delayMillis(long millis);

    @ActivityMethod
    String httpRequest(String method, String url);

    @ActivityMethod
    void sendEmail(String to, String subject, String body);
}
