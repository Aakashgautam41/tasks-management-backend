package com.example.tasks_management_backend.controller;

import com.example.tasks_management_backend.dto.ApiResponse;
import com.example.tasks_management_backend.dto.SubTaskRequest;
import com.example.tasks_management_backend.model.SubTask;
import com.example.tasks_management_backend.service.TaskService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/subtasks")
public class SubTaskController {

    @Autowired
    private TaskService taskService;

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SubTask>> updateSubTask(@PathVariable Long id,
            @Valid @RequestBody SubTaskRequest subTaskRequest) {
        SubTask subTask = new SubTask();
        subTask.setId(id);
        subTask.setTitle(subTaskRequest.title());
        subTask.setPriority(subTaskRequest.priority());
        subTask.setDeadline(subTaskRequest.deadline());
        subTask.setStatus(subTaskRequest.status());

        SubTask updated = taskService.updateSubTask(subTask);
        if (updated == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, "SubTask not found", null));
        }
        return ResponseEntity.ok(new ApiResponse<>(true, 200, "SubTask updated successfully", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSubTask(@PathVariable Long id) {
        try {
            taskService.deleteSubTask(id);
            return ResponseEntity.ok(new ApiResponse<>(true, 200, "SubTask deleted successfully", null));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new ApiResponse<>(false, 404, "SubTask not found", null));
        }
    }
}
