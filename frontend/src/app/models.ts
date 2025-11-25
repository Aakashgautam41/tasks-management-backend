export interface ApiResponse<T> {
    success: boolean;
    status: number;
    message: string;
    data: T;
}

export interface AuthRequest {
    username: string;
    password?: string;
}

export interface AuthResponse {
    token: string;
}

export interface UserRegistrationRequest {
    username: string;
    password?: string;
    email: string;
}

export interface Task {
    id?: number;
    title: string;
    priority: 'LOW' | 'MEDIUM' | 'HIGH';
    deadline?: string;
    status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
    subtasks?: SubTask[];
}

export interface SubTask {
    id?: number;
    title: string;
    priority: 'LOW' | 'MEDIUM' | 'HIGH';
    deadline?: string;
    status: 'PENDING' | 'IN_PROGRESS' | 'COMPLETED' | 'CANCELLED';
}

export interface Page<T> {
    content: T[];
    totalPages: number;
    totalElements: number;
    size: number;
    number: number;
    first: boolean;
    last: boolean;
    empty: boolean;
}
