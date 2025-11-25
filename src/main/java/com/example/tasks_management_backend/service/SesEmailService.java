package com.example.tasks_management_backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.model.*;

@Service
public class SesEmailService implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(SesEmailService.class);

    private final SesClient sesClient;

    @Value("${aws.ses.sourceEmail}")
    private String sourceEmail;

    public SesEmailService(SesClient sesClient) {
        this.sesClient = sesClient;
    }

    @Override
    public void sendTaskCompletedEmail(String recipient, String taskTitle, Long taskId) {
        try {
            SendEmailRequest emailRequest = SendEmailRequest.builder()
                    .destination(Destination.builder().toAddresses(recipient).build())
                    .message(Message.builder()
                            .subject(Content.builder().data("Task Completed: " + taskTitle).build())
                            .body(Body.builder()
                                    .text(Content.builder()
                                            .data("The task '" + taskTitle + "' (ID: " + taskId
                                                    + ") has been automatically marked as completed.")
                                            .build())
                                    .build())
                            .build())
                    .source(sourceEmail)
                    .build();

            sesClient.sendEmail(emailRequest);
            logger.info("Sent completion email for task ID {} to {}", taskId, recipient);
        } catch (SesException e) {
            logger.error("Failed to send email via SES", e);
        }
    }
}
