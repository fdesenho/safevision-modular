import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { NotificationService } from '../../../core/services/notification.service'; // <--- SEU NOVO SERVIÇO

// Módulos Visuais
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
   
  ],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss'
})
export class LoginComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  
  // ✅ Injetamos nosso serviço limpo, não o MatSnackBar direto
  private notifier = inject(NotificationService);

  isLoading = signal(false);
  hidePassword = signal(true); 

  loginForm = this.fb.nonNullable.group({
    username: ['', Validators.required],
    password: ['', Validators.required]
  });

  togglePassword(event: Event) {
    event.preventDefault(); 
    this.hidePassword.update(value => !value);
  }

  onSubmit() {
    if (this.loginForm.invalid) return;

    this.isLoading.set(true);
    const credentials = this.loginForm.getRawValue();

    this.authService.login(credentials).subscribe({
      next: () => {
        this.router.navigate(['/dashboard']);
        
        this.notifier.showSuccess('Bem-vindo ao SafeVision!');
      },
      error: (err) => {
        this.isLoading.set(false);
        
        const mensagem = err.status === 401 || err.status === 403
          ? 'Usuário ou senha incorretos!'
          : 'Servidor indisponível.';

        // ✅ Chamada limpa e reutilizável
        this.notifier.showError(mensagem);
      }
    });
  }
}