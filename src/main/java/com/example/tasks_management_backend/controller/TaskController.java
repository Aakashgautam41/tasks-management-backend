package com.example.tasks_management_backend.controller;

import com.example.tasks_management_backend.dto.ApiResponse;
import com.example.tasks_management_backend.dto.SubTaskRequest;
import com.example.tasks_management_backend.dto.TaskRequest;
import com.example.tasks_management_backend.model.SubTask;
import com.example.tasks_management_backend.model.Task;
import com.example.tasks_management_backend.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    @Autowired
    private TaskService taskService;

    @GetMapping
    public ResponseEntity<ApiResponse<Page<Task>>> getTasks(
            @RequestParam(required = false) Task.Priority priority,
            @RequestParam(required = false) Task.Status status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate deadlineBefore,
            @RequestParam(required = false) String sortBy,
            @RequestParam(required = false, defaultValue = "asc") String direction,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        Page<Task> tasksPage = taskService.getTasks(priority, status, deadlineBefore, sortBy, direction, page, size);
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Tasks retrieved successfully", tasksPage));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Task>> createTask(@Valid @RequestBody TaskRequest taskRequest) {
        Task task = new Task();
        task.setTitle(taskRequest.title());
        task.setPriority(taskRequest.priority());
        task.setDeadline(taskRequest.deadline());
        task.setStatus(taskRequest.status());

        if (taskRequest.subtasks() != null) {
            List<SubTask> subTasks = taskRequest.subtasks().stream().map(stDto -> {
                SubTask st = new SubTask();
                st.setTitle(stDto.title());
                st.setPriority(stDto.priority());
                st.setDeadline(stDto.deadline());
                st.setStatus(stDto.status());
                return st;
            }).collect(Collectors.toList());
            task.setSubtasks(subTasks);
        }

        Task saved = taskService.saveTask(task);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, 201, "Task created successfully", saved));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> getTask(@PathVariable Long id) {
        Task task = taskService.getTask(id);
        if (task == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, "Task not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Task retrieved successfully", task));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Task>> updateTask(@PathVariable Long id,
            @Valid @RequestBody TaskRequest taskRequest) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(taskRequest.title());
        task.setPriority(taskRequest.priority());
        task.setDeadline(taskRequest.deadline());
        task.setStatus(taskRequest.status());

        if (taskRequest.subtasks() != null) {
            List<SubTask> subTasks = taskRequest.subtasks().stream().map(stDto -> {
                SubTask st = new SubTask();
                st.setId(stDto.id());
                st.setTitle(stDto.title());
                st.setPriority(stDto.priority());
                st.setDeadline(stDto.deadline());
                st.setStatus(stDto.status());
                return st;
            }).collect(Collectors.toList());
            task.setSubtasks(subTasks);
        }

        Task updated = taskService.updateTask(task);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, "Task not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Task updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteTask(@PathVariable Long id) {
        try {
            taskService.deleteTask(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "Task deleted successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, "Task not found", null));
        }
    }

    @PostMapping("/{taskId}/subtasks")
    public ResponseEntity<ApiResponse<SubTask>> createSubTask(@PathVariable Long taskId,
            @Valid @RequestBody SubTaskRequest subTaskRequest) {
        SubTask subTask = new SubTask();
        subTask.setTitle(subTaskRequest.title());
        subTask.setPriority(subTaskRequest.priority());
        subTask.setDeadline(subTaskRequest.deadline());
        subTask.setStatus(subTaskRequest.status());

        SubTask created = taskService.createSubTask(taskId, subTask);
        if (created == null) {
            // Possible that taskId not found or creation failed
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(new ApiResponse<>(false, 400, "Failed to create subtask. Task ID might be invalid.", null));
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>(true, 201, "SubTask created successfully", created));
    }

    @GetMapping("/{taskId}/subtasks")
    public ResponseEntity<ApiResponse<List<SubTask>>> getSubtasksForTask(@PathVariable Long taskId) {
        List<SubTask> subTasks = taskService.getSubTasksByTaskId(taskId);
        if (subTasks == null || subTasks.isEmpty()) {
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "No subtasks found", subTasks));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "Subtasks retrieved successfully", subTasks));
    }

}