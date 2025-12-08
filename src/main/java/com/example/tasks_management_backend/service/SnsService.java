package com.example.tasks_management_backend.service;

import com.example.tasks_management_backend.model.Task;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;

import java.util.HashMap;
import java.util.Map;

@Service
public class SnsService {

    private static final Logger logger = LoggerFactory.getLogger(SnsService.class);

    private final SnsClient snsClient;
    private final String topicArn;
    private final ObjectMapper objectMapper;

    public SnsService(SnsClient snsClient, @Value("${aws.sns.topicArn}") String topicArn) {
        this.snsClient = snsClient;
        this.topicArn = topicArn;
        this.objectMapper = new ObjectMapper();
    }

    public void publishTaskCreatedEvent(Task task) {
        try {
            String message = createMessage(task);
            PublishRequest request = PublishRequest.builder()
                    .topicArn(topicArn)
                    .message(message)
                    .subject("High Priority Task Created: " + task.getTitle())
                    .build();

            PublishResponse result = snsClient.publish(request);
            logger.info("Published High Priority Task event to SNS. MessageId: {}, TaskId: {}", result.messageId(),
                    task.getId());
        } catch (Exception e) {
            logger.error("Failed to publish SNS message for task {}", task.getId(), e);
        }
    }

    private String createMessage(Task task) throws JsonProcessingException {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("taskId", task.getId());
        eventData.put("title", task.getTitle());
        eventData.put("priority", task.getPriority());
        eventData.put("status", task.getStatus());
        eventData.put("deadline", task.getDeadline() != null ? task.getDeadline().toString() : null);
        if (task.getUser() != null) {
            eventData.put("assignedUser", task.getUser().getUsername());
        }

        return objectMapper.writeValueAsString(eventData);
    }
}
