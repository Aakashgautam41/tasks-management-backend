import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TaskService } from '../../services/task/task';
import { Task } from '../../models';

@Component({
  selector: 'app-task-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './task-form.html',
  styleUrl: './task-form.css'
})
export class TaskFormComponent implements OnInit {
  taskForm: FormGroup;
  isEditMode: boolean = false;
  taskId: number | null = null;
  isLoading: boolean = false;
  error: string = '';

  constructor(
    private fb: FormBuilder,
    private taskService: TaskService,
    private router: Router,
    private route: ActivatedRoute
  ) {
    this.taskForm = this.fb.group({
      title: ['', [Validators.required, Validators.minLength(3)]],
      priority: ['MEDIUM', Validators.required],
      status: ['PENDING', Validators.required],
      deadline: ['', [Validators.required]]
    });
  }

  ngOnInit(): void {
    this.route.params.subscribe(params => {
      if (params['id']) {
        this.isEditMode = true;
        this.taskId = +params['id'];
        this.loadTask(this.taskId);
      }
    });
  }

  loadTask(id: number): void {
    this.isLoading = true;
    this.taskService.getTask(id).subscribe({
      next: (response) => {
        if (response.success) {
          const task = response.data;
          this.taskForm.patchValue({
            title: task.title,
            priority: task.priority,
            status: task.status,
            deadline: task.deadline
          });
        }
        this.isLoading = false;
      },
      error: () => {
        this.error = 'Failed to load task';
        this.isLoading = false;
      }
    });
  }

  onSubmit(): void {
    if (this.taskForm.valid) {
      this.isLoading = true;
      const taskData: Task = this.taskForm.value;

      if (this.isEditMode && this.taskId) {
        this.taskService.updateTask(this.taskId, taskData).subscribe({
          next: () => this.router.navigate(['/dashboard']),
          error: () => {
            this.error = 'Failed to update task';
            this.isLoading = false;
          }
        });
      } else {
        this.taskService.createTask(taskData).subscribe({
          next: () => this.router.navigate(['/dashboard']),
          error: () => {
            this.error = 'Failed to create task';
            this.isLoading = false;
          }
        });
      }
    }
  }
}
