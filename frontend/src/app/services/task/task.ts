import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiResponse, Page, Task, SubTask } from '../../models';

@Injectable({
  providedIn: 'root'
})
export class TaskService {
  private apiUrl = 'http://localhost:8080/api';

  constructor(private http: HttpClient) { }

  getTasks(page: number = 0, size: number = 10, priority?: string, status?: string, sortBy?: string): Observable<ApiResponse<Page<Task>>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());

    if (priority) params = params.set('priority', priority);
    if (status) params = params.set('status', status);
    if (sortBy) params = params.set('sortBy', sortBy);

    return this.http.get<ApiResponse<Page<Task>>>(`${this.apiUrl}/tasks`, { params });
  }

  getTask(id: number): Observable<ApiResponse<Task>> {
    return this.http.get<ApiResponse<Task>>(`${this.apiUrl}/tasks/${id}`);
  }

  createTask(task: Task): Observable<ApiResponse<Task>> {
    return this.http.post<ApiResponse<Task>>(`${this.apiUrl}/tasks`, task);
  }

  updateTask(id: number, task: Task): Observable<ApiResponse<Task>> {
    return this.http.put<ApiResponse<Task>>(`${this.apiUrl}/tasks/${id}`, task);
  }

  deleteTask(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/tasks/${id}`);
  }

  createSubTask(taskId: number, subTask: SubTask): Observable<ApiResponse<SubTask>> {
    return this.http.post<ApiResponse<SubTask>>(`${this.apiUrl}/tasks/${taskId}/subtasks`, subTask);
  }

  updateSubTask(id: number, subTask: SubTask): Observable<ApiResponse<SubTask>> {
    return this.http.put<ApiResponse<SubTask>>(`${this.apiUrl}/subtasks/${id}`, subTask);
  }

  deleteSubTask(id: number): Observable<ApiResponse<void>> {
    return this.http.delete<ApiResponse<void>>(`${this.apiUrl}/subtasks/${id}`);
  }
}
