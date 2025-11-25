package com.example.tasks_management_backend.service;

import com.example.tasks_management_backend.model.Task;
import com.example.tasks_management_backend.model.User;
import com.example.tasks_management_backend.model.SubTask;
import com.example.tasks_management_backend.repository.TaskRepository;
import com.example.tasks_management_backend.repository.SubTaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private SubTaskRepository subTaskRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private TaskService taskService;

    private Task task;
    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");

        task = new Task();
        task.setId(1L);
        task.setTitle("Test Task");
        task.setStatus(Task.Status.PENDING);
        task.setPriority(Task.Priority.MEDIUM);
        task.setDeadline(LocalDate.now().plusDays(1));
        task.setUser(user);
    }

    @Test
    void saveTask_Success() {
        when(userService.getCurrentUser()).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task savedTask = taskService.saveTask(task);

        assertNotNull(savedTask);
        assertEquals("Test Task", savedTask.getTitle());
        verify(taskRepository, times(1)).save(task);
    }

    @Test
    void getTasks_Filtering() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Task> taskPage = new PageImpl<>(Collections.singletonList(task));

        when(taskRepository.findAll(any(org.springframework.data.jpa.domain.Specification.class), any(Pageable.class)))
                .thenReturn(taskPage);

        Page<Task> result = taskService.getTasks(Task.Priority.MEDIUM, Task.Status.PENDING, null, null, null, 0, 10);

        assertEquals(1, result.getTotalElements());
        assertEquals(Task.Status.PENDING, result.getContent().get(0).getStatus());
    }

    @Test
    void updateTask_Success() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        Task updatedTask = new Task();
        updatedTask.setId(1L);
        updatedTask.setTitle("Updated Title");

        Task result = taskService.updateTask(updatedTask);

        assertEquals("Updated Title", result.getTitle());
    }

    @Test
    void updateTask_NotFound() {
        when(taskRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> taskService.updateTask(task));
    }

    @Test
    void createSubTask_Success() {
        SubTask subTask = new SubTask();
        subTask.setTitle("SubTask 1");

        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(user);
        when(subTaskRepository.save(any(SubTask.class))).thenReturn(subTask);

        SubTask result = taskService.createSubTask(1L, subTask);

        assertNotNull(result);
        assertEquals("SubTask 1", result.getTitle());
        verify(subTaskRepository, times(1)).save(subTask);
    }

    @Test
    void updateTask_WithSubTasks() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(task));
        when(userService.getCurrentUser()).thenReturn(user);
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        SubTask existingSubTask = new SubTask();
        existingSubTask.setId(10L);
        existingSubTask.setTitle("Old Title");
        task.addSubTask(existingSubTask);

        Task updateRequest = new Task();
        updateRequest.setId(1L);
        updateRequest.setTitle("Updated Task");

        SubTask subTaskUpdate = new SubTask();
        subTaskUpdate.setId(10L);
        subTaskUpdate.setTitle("New Title");

        SubTask newSubTask = new SubTask();
        newSubTask.setTitle("New SubTask");

        updateRequest.setSubtasks(List.of(subTaskUpdate, newSubTask));

        taskService.updateTask(updateRequest);

        assertEquals("New Title", existingSubTask.getTitle());
        assertEquals(2, task.getSubtasks().size());
    }
}
