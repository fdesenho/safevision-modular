import { Component, inject } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth.service';
import { HttpErrorResponse } from '@angular/common/http';

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss'
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);

  registerForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6)]],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', Validators.required],
    cameraUrl: ['']
  });

  isLoading = false;
  errorMessage = '';
  successMessage = '';

  onSubmit() {
    if (this.registerForm.invalid) return;

    this.isLoading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.authService.register(this.registerForm.value as any).subscribe({
      next: () => {
        this.isLoading = false;
        this.successMessage = 'Conta criada com sucesso! Redirecionando...';
        setTimeout(() => this.router.navigate(['/login']), 2000);
      },
      // --- AQUI ESTÁ A MÁGICA DO CLEAN CODE ---
      error: (err: Error) => {
        this.isLoading = false;
        // O Interceptor já tratou e nos mandou a mensagem limpa no err.message
        this.errorMessage = err.message;
      }
      // ----------------------------------------
    });
  }

  // --- MÉTODO DE EXTRAÇÃO MELHORADO ---
  private extractErrorMessage(err: HttpErrorResponse): string {
    // 1. Se for erro de conexão (servidor desligado)
    if (err.status === 0) {
      return 'Servidor indisponível. Verifique sua conexão.';
    }

    // 2. Se o backend mandou o nosso formato customizado: { "error": "Mensagem..." }
    if (err.error && typeof err.error === 'object' && err.error.error) {
      return err.error.error;
    }

    // 3. Se o Spring Security mandou o padrão dele: { "message": "Bad Credentials" }
    if (err.error && typeof err.error === 'object' && err.error.message) {
      return err.error.message;
    }

    // 4. Se o backend mandou apenas texto puro
    if (typeof err.error === 'string') {
      return err.error;
    }

    // 5. Fallback: Usa o texto padrão do HTTP (Ex: "400 Bad Request")
    return err.statusText || 'Ocorreu um erro desconhecido.';
  }
}
