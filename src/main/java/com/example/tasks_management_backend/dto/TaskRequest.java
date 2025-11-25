package com.example.tasks_management_backend.dto;

import com.example.tasks_management_backend.model.Task;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record TaskRequest(
        @NotBlank(message = "Title is mandatory") @Size(min = 3, max = 255, message = "Title must be between 3 and 255 characters") String title,

        @NotNull(message = "Priority is required") Task.Priority priority,

        @FutureOrPresent(message = "Deadline cannot be in the past") LocalDate deadline,

        @NotNull(message = "Status is required") Task.Status status,

        List<SubTaskRequest> subtasks) {
}
