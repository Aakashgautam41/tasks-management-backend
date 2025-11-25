package com.example.tasks_management_backend.validations;

import com.example.tasks_management_backend.dto.ApiResponse;
import com.example.tasks_management_backend.controller.AuthController;
import com.example.tasks_management_backend.controller.SubTaskController;
import com.example.tasks_management_backend.controller.TaskController;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import com.example.tasks_management_backend.controller.AuthController;
import com.example.tasks_management_backend.controller.SubTaskController;
import com.example.tasks_management_backend.controller.TaskController;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice(assignableTypes = { TaskController.class, SubTaskController.class, AuthController.class })
public class ValidationExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            errors.put(fieldName, message);
        });

        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, 400, "Validation failed", errors));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ResponseEntity<ApiResponse<Void>> handleJsonParseException(HttpMessageNotReadableException ex) {
        String msg = ex.getMostSpecificCause().getMessage();
        Map<String, String> error = new HashMap<>();

        if (msg.contains("Priority")) {
            error.put("priority", "Invalid priority. Allowed values: LOW, MEDIUM, HIGH");
        } else if (msg.contains("Status")) {
            error.put("status", "Invalid status. Allowed values: PENDING, IN_PROGRESS, COMPLETED, CANCELLED");
        } else {
            error.put("error", "Malformed JSON or invalid field.");
        }
        return ResponseEntity.badRequest()
                .body(new ApiResponse<>(false, 400, "Malformed JSON request", error));
    }

    @ExceptionHandler(TransactionSystemException.class)
    public ResponseEntity<ApiResponse<Void>> handleTransactionSystemException(TransactionSystemException ex) {
        if (ex.getRootCause() instanceof ConstraintViolationException cve) {
            Map<String, String> errors = cve.getConstraintViolations().stream()
                    .collect(Collectors.toMap(
                            v -> v.getPropertyPath().toString(),
                            ConstraintViolation::getMessage));
            return ResponseEntity.badRequest()
                    .body(new ApiResponse<>(false, 400, "Database validation failed", errors));
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiResponse<>(false, 500, "Transaction failed", null));
    }
}