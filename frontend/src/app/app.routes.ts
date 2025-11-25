import { Routes } from '@angular/router';
import { LoginComponent } from './components/login/login';
import { RegisterComponent } from './components/register/register';
import { DashboardComponent } from './components/dashboard/dashboard';
import { TaskFormComponent } from './components/task-form/task-form';
import { TaskDetailComponent } from './components/task-detail/task-detail';
import { authGuard } from './guards/auth-guard';

export const routes: Routes = [
    { path: 'login', component: LoginComponent },
    { path: 'register', component: RegisterComponent },
    {
        path: 'dashboard',
        component: DashboardComponent,
        canActivate: [authGuard]
    },
    {
        path: 'tasks/new',
        component: TaskFormComponent,
        canActivate: [authGuard]
    },
    {
        path: 'tasks/:id',
        component: TaskDetailComponent,
        canActivate: [authGuard]
    },
    {
        path: 'tasks/:id/edit',
        component: TaskFormComponent,
        canActivate: [authGuard]
    },
    { path: '', redirectTo: '/dashboard', pathMatch: 'full' }
];
