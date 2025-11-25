package com.example.tasks_management_backend.repository;

import com.example.tasks_management_backend.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TaskRepository extends JpaRepository<Task, Long>, JpaSpecificationExecutor<Task> {
    @Query("SELECT t FROM Task t LEFT JOIN FETCH t.subtasks WHERE t.id = :id")
    Optional<Task> findByIdWithSubtasks(@Param("id")Long id);

    // Tasks with deadline exactly on the specified date
    List<Task> findByDeadline(LocalDate deadline);

    // Tasks that are overdue: deadline before given date, and with specific status
    List<Task> findByDeadlineBeforeAndStatus(LocalDate date, Task.Status status);

    List<Task> findByStatus(Task.Status status);
}