package com.example.tasks_management_backend.repository;

import com.example.tasks_management_backend.model.SubTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface SubTaskRepository extends JpaRepository<SubTask, Long>, JpaSpecificationExecutor<SubTask> {
    List<SubTask> findByParentTaskId(Long taskId);
}
