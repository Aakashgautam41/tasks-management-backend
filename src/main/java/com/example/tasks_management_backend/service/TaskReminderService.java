package com.example.tasks_management_backend.service;

import com.example.tasks_management_backend.model.Task;
import com.example.tasks_management_backend.repository.TaskRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TaskReminderService {
    private static final Logger logger = LoggerFactory.getLogger(TaskReminderService.class);

    private final TaskRepository taskRepository;
    private final EmailService emailService;

    public TaskReminderService(TaskRepository taskRepository, EmailService emailService) {
        this.taskRepository = taskRepository;
        this.emailService = emailService;
    }

    // Run every 2 minutes
    @Transactional
    @Scheduled(cron = "${app.task.cron}")
    public void markPendingTasksComplete() {
        List<Task> pendingTasks = taskRepository.findByStatus(Task.Status.PENDING);

        for (Task task : pendingTasks) {
            task.setStatus(Task.Status.COMPLETED);
            taskRepository.save(task);
            logger.info("Marked task with id={} and title='{}' as COMPLETED", task.getId(), task.getTitle());

            // In a real app, we would get the user's email from the task or associated user
            // For this demo, we'll use a placeholder or the source email if configured
            // emailService.sendTaskCompletedEmail("user@example.com", task.getTitle(),
            // task.getId());
            emailService.sendTaskCompletedEmail("recipient@example.com", task.getTitle(), task.getId());
        }
    }
}
