package com.example.tasks_management_backend.service;

public interface EmailService {
    void sendTaskCompletedEmail(String recipient, String taskTitle, Long taskId);
}
