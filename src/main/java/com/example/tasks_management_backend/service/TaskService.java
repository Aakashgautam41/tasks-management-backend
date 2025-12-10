package com.example.tasks_management_backend.service;

import com.example.tasks_management_backend.model.SubTask;
import com.example.tasks_management_backend.model.Task;
import com.example.tasks_management_backend.model.User;
import com.example.tasks_management_backend.repository.SubTaskRepository;
import com.example.tasks_management_backend.repository.TaskRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import org.springframework.web.multipart.MultipartFile;
import java.util.Optional;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final SubTaskRepository subTaskRepository;
    private final UserService userService;
    private final SnsService snsService;
    // private final KafkaProducerService kafkaProducerService;
    private final S3Service s3Service;

    public TaskService(TaskRepository taskRepository, SubTaskRepository subTaskRepository, UserService userService,
            SnsService snsService, S3Service s3Service /* , KafkaProducerService kafkaProducerService */) {
        this.taskRepository = taskRepository;
        this.subTaskRepository = subTaskRepository;
        this.userService = userService;
        this.snsService = snsService;
        this.s3Service = s3Service;
        // this.kafkaProducerService = kafkaProducerService;
    }

    @CachePut(value = "tasks", key = "#task.id")
    @CacheEvict(value = "tasks", allEntries = true)
    public Task saveTask(Task task) {
        task.setUser(userService.getCurrentUser());
        if (task.getSubtasks() != null) {
            task.getSubtasks().forEach(st -> st.setParentTask(task));
        }
        Task savedTask = taskRepository.save(task);

        if (savedTask.getPriority() == Task.Priority.HIGH) {
            snsService.publishTaskCreatedEvent(savedTask);
        }
        // kafkaProducerService.sendTaskEvent("task-created", savedTask);

        return savedTask;
    }

    @Transactional
    @CachePut(value = "tasks", key = "#task.id")
    @CacheEvict(value = "tasks", allEntries = true)
    public Task updateTask(Task task) {
        Task existingTask = taskRepository.findById(task.getId())
                .orElseThrow(() -> new EntityNotFoundException("Task not found"));

        validateTaskOwnership(existingTask);

        existingTask.setTitle(task.getTitle());
        existingTask.setPriority(task.getPriority());
        existingTask.setDeadline(task.getDeadline());
        existingTask.setStatus(task.getStatus());

        if (task.getSubtasks() != null) {
            List<Long> incomingIds = task.getSubtasks().stream()
                    .map(SubTask::getId)
                    .filter(Objects::nonNull)
                    .toList();

            existingTask.getSubtasks().removeIf(existingSubTask -> existingSubTask.getId() != null
                    && !incomingIds.contains(existingSubTask.getId()));

            for (SubTask incomingSubTask : task.getSubtasks()) {
                if (incomingSubTask.getId() != null) {
                    existingTask.getSubtasks().stream()
                            .filter(st -> st.getId().equals(incomingSubTask.getId()))
                            .findFirst()
                            .ifPresent(existingSubTask -> {
                                existingSubTask.setTitle(incomingSubTask.getTitle());
                                existingSubTask.setPriority(incomingSubTask.getPriority());
                                existingSubTask.setDeadline(incomingSubTask.getDeadline());
                                existingSubTask.setStatus(incomingSubTask.getStatus());
                            });
                } else {
                    existingTask.addSubTask(incomingSubTask);
                }
            }
        } else {
            existingTask.getSubtasks().clear();
        }

        Task updatedTask = taskRepository.save(existingTask);

        if (updatedTask.getPriority() == Task.Priority.HIGH) {
            snsService.publishTaskCreatedEvent(updatedTask);
        }

        return updatedTask;
    }

    public Task getTask(Long id) {
        Task task = taskRepository.findById(id).orElse(null);
        if (task != null) {
            validateTaskOwnership(task);
        }
        return task;
    }

    @Cacheable(value = "tasks", key = "T(java.util.Objects).hash(#priority, #status, #deadlineBefore, #sortBy, #direction, #page, #size, @userService.currentUser.id)")
    public Page<Task> getTasks(
            Task.Priority priority,
            Task.Status status,
            LocalDate deadlineBefore,
            String sortBy,
            String direction,
            int page,
            int size) {

        Sort sort = Sort.by(Sort.Direction.fromString(direction != null ? direction : "ASC"),
                sortBy != null ? sortBy : "id");

        Pageable pageable = PageRequest.of(page, size, sort);

        Specification<Task> spec = (root, query, criteriaBuilder) -> criteriaBuilder.conjunction();

        // Filter by current user
        spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("user"),
                userService.getCurrentUser()));

        if (priority != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("priority"), priority));
        }
        if (status != null) {
            spec = spec.and((root, query, criteriaBuilder) -> criteriaBuilder.equal(root.get("status"), status));
        }
        if (deadlineBefore != null) {
            spec = spec.and(
                    (root, query, criteriaBuilder) -> criteriaBuilder.lessThan(root.get("deadline"), deadlineBefore));
        }

        return taskRepository.findAll(spec, pageable);
    }

    @Transactional
    @CacheEvict(value = "tasks", allEntries = true)
    public void deleteTask(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id " + id));
        validateTaskOwnership(task);
        taskRepository.delete(task);
    }

    @Transactional()
    public Optional<Task> getTaskWithSubTasks(Long id) {
        Optional<Task> task = taskRepository.findByIdWithSubtasks(id);
        task.ifPresent(this::validateTaskOwnership);
        return task;
    }

    @Transactional
    public SubTask createSubTask(Long taskId, SubTask subTask) {
        Task parent = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        validateTaskOwnership(parent);

        subTask.setParentTask(parent);
        return subTaskRepository.save(subTask);
    }

    @Transactional
    public SubTask updateSubTask(SubTask updatedSubTask) {
        SubTask existingSubTask = subTaskRepository.findById(updatedSubTask.getId())
                .orElseThrow(() -> new EntityNotFoundException("SubTask not found"));

        validateTaskOwnership(existingSubTask.getParentTask());

        existingSubTask.setTitle(updatedSubTask.getTitle());
        existingSubTask.setPriority(updatedSubTask.getPriority());
        existingSubTask.setStatus(updatedSubTask.getStatus());
        existingSubTask.setDeadline(updatedSubTask.getDeadline());

        return subTaskRepository.save(existingSubTask);
    }

    @Transactional
    public List<SubTask> getSubTasksByTaskId(Long taskId) {
        Task parent = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));
        validateTaskOwnership(parent);
        return subTaskRepository.findByParentTaskId(taskId);
    }

    @Transactional
    public void deleteSubTask(Long subTaskId) {
        SubTask subTask = subTaskRepository.findById(subTaskId)
                .orElseThrow(() -> new EntityNotFoundException("SubTask not found with id: " + subTaskId));
        validateTaskOwnership(subTask.getParentTask());
        subTaskRepository.deleteById(subTaskId);
    }

    public Task uploadAttachment(Long taskId, MultipartFile file) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new EntityNotFoundException("Task not found with id: " + taskId));

        // Security check: ensure current user owns the task
        validateTaskOwnership(task);

        String attachmentUrl = s3Service.uploadFile(file);
        task.setAttachmentUrl(attachmentUrl);
        return taskRepository.save(task);
    }

    private void validateTaskOwnership(Task task) {
        User currentUser = userService.getCurrentUser();
        if (!task.getUser().getId().equals(currentUser.getId())) {
            org.slf4j.LoggerFactory.getLogger(TaskService.class).error(
                    "Access denied. Task owner ID: {}, Current user ID: {}", task.getUser().getId(),
                    currentUser.getId());
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have permission to access this task");
        }
    }
}
