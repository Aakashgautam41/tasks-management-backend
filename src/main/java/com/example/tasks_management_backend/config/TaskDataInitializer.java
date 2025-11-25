package com.example.tasks_management_backend.config;

import com.example.tasks_management_backend.model.Role;
import com.example.tasks_management_backend.model.SubTask;
import com.example.tasks_management_backend.model.Task;
import com.example.tasks_management_backend.model.User;
import com.example.tasks_management_backend.repository.TaskRepository;
import com.example.tasks_management_backend.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Random;
import java.util.Set;

@Component
public class TaskDataInitializer implements CommandLineRunner {

    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Random random = new Random();

    private static final Task.Priority[] PRIORITIES = Task.Priority.values();
    private static final Task.Status[] STATUSES = Task.Status.values();

    public TaskDataInitializer(TaskRepository taskRepository, UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.taskRepository = taskRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Create a default user
        if (!userRepository.existsByUsername("admin")) {
            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@example.com");
            admin.setRoles(Set.of(Role.ROLE_ADMIN, Role.ROLE_USER));
            userRepository.save(admin);
        }

        User defaultUser = userRepository.findByUsername("admin").orElseThrow();

        for (int i = 1; i <= 10; i++) {
            Task task = new Task();
            task.setTitle("Task " + i);
            task.setUser(defaultUser);

            // Random priority and status
            task.setPriority(randomPriority());
            task.setStatus(randomStatus());

            // Random deadline between today and 10 days from now
            task.setDeadline(LocalDate.now().plusDays(random.nextInt(11)));

            // Optionally add 1-3 subtasks
            int numSubtasks = 1 + random.nextInt(3);
            for (int j = 1; j <= numSubtasks; j++) {
                SubTask subTask = new SubTask();
                subTask.setTitle("Subtask " + j + " for Task " + i);

                subTask.setPriority(randomPriority());
                subTask.setStatus(randomStatus());
                subTask.setDeadline(LocalDate.now().plusDays(random.nextInt(11)));

                // Set parent task reference for proper cascading
                subTask.setParentTask(task);

                // Add subtask to task's list
                task.getSubtasks().add(subTask);
            }

            taskRepository.save(task);
        }
    }

    private Task.Priority randomPriority() {
        return PRIORITIES[random.nextInt(PRIORITIES.length)];
    }

    private Task.Status randomStatus() {
        return STATUSES[random.nextInt(STATUSES.length)];
    }
}