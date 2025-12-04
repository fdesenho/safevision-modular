import { Routes } from '@angular/router';
import { LoginComponent } from './features/auth/login/login.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';
import { RegisterComponent } from './features/auth/register/register.component';
import { authGuard } from './core/guards/auth-guard';

export const routes: Routes = [
  // Rota padrão redireciona para login
  { path: '', redirectTo: 'login', pathMatch: 'full' },

  // Rota de Login
  { path: 'login', component: LoginComponent },

  // Rota de Registro
  { path: 'register', component: RegisterComponent },

  // Rota Protegida
  {
    path: 'dashboard',
    component: DashboardComponent,
    canActivate: [authGuard]
  },

  // (Opcional) Rota Curinga: Se digitar algo errado, vai pro login
  { path: '**', redirectTo: 'login' }
];
