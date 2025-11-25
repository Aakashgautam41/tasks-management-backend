import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { TaskService } from '../../services/task/task';
import { Task, SubTask } from '../../models';

@Component({
  selector: 'app-task-detail',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './task-detail.html',
  styleUrl: './task-detail.css'
})
export class TaskDetailComponent implements OnInit {
  task: Task | null = null;
  subTasks: SubTask[] = [];
  subTaskForm: FormGroup;
  isLoading: boolean = false;
  error: string = '';
  taskId: number | null = null;

  constructor(
    private route: ActivatedRoute,
    private taskService: TaskService,
    private fb: FormBuilder
  ) {
    this.subTaskForm = this.fb.group({
      title: ['', Validators.required],
      priority: ['MEDIUM', Validators.required],
      status: ['PENDING', Validators.required],
      deadline: ['']
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.taskId = +params['id'];
        this.loadTask(this.taskId);
        this.loadSubTasks(this.taskId);
      }
    });
  }

  loadTask(id: number): void {
    this.isLoading = true;
    this.taskService.getTask(id).subscribe({
      next: (response) => {
        if (response.success) {
          this.task = response.data;
        }
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load task';
        this.isLoading = false;
      }
    });
  }

  loadSubTasks(taskId: number): void {
    this.taskService.getTask(taskId).subscribe(response => { // Re-using getTask as it returns subtasks too usually, but let's check if there is a specific endpoint.
      // Actually the backend has getSubtasksForTask endpoint: GET /api/tasks/{taskId}/subtasks
      // Let's use that if available or rely on task.subtasks if eager loaded.
      // Based on controller: @GetMapping("/{taskId}/subtasks")
    });

    // Correct approach:
    this.taskService.getTask(taskId).subscribe(response => {
      if (response.success && response.data.subtasks) {
        this.subTasks = response.data.subtasks;
      }
    });
  }

  addSubTask(): void {
    if (this.subTaskForm.valid && this.taskId) {
      this.taskService.createSubTask(this.taskId, this.subTaskForm.value).subscribe({
        next: (response) => {
          if (response.success) {
            this.subTasks.push(response.data);
            this.subTaskForm.reset({ priority: 'MEDIUM', status: 'PENDING' });
          }
        }
      });
    }
  }

  deleteSubTask(subTask: SubTask): void {
    if (confirm('Delete this subtask?') && subTask.id) {
      this.taskService.deleteSubTask(subTask.id).subscribe(() => {
        this.subTasks = this.subTasks.filter(st => st.id !== subTask.id);
      });
    }
  }

  updateSubTaskStatus(subTask: SubTask, status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED'): void {
    if (subTask.id) {
      const updated = { ...subTask, status };
      this.taskService.updateSubTask(subTask.id, updated).subscribe(response => {
        if (response.success) {
          const index = this.subTasks.findIndex(st => st.id === subTask.id);
          if (index !== -1) {
            this.subTasks[index] = response.data;
          }
        }
      });
    }
  }
}
