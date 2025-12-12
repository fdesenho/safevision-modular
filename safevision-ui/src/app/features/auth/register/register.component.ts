import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { CommonModule } from '@angular/common';

// Imports do Material
import { MatCardModule } from '@angular/material/card';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatCheckboxModule } from '@angular/material/checkbox';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatChipsModule } from '@angular/material/chips';

// Enum para tipagem forte
export enum AlertType {
  TELEGRAM = 'TELEGRAM',
  EMAIL = 'EMAIL',
  SMS = 'SMS'
}

@Component({
  selector: 'app-register',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule, MatInputModule, MatButtonModule, MatIconModule,
    MatCheckboxModule, MatProgressSpinnerModule, MatSnackBarModule,
    MatChipsModule
  ],
  templateUrl: './register.component.html',
  styleUrls: ['./register.component.scss']
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private router = inject(Router);
  private snackBar = inject(MatSnackBar);

  // Controle de Estado da UI
  isLoading = signal<boolean>(false);
  
  // ✅ NOVO: Controla se mostramos o form ou a tela de "Check" verde
  isRegisterSuccess = signal<boolean>(false);

  // ❌ REMOVIDOS: successMessage e errorMessage (para evitar duplicação visual com o SnackBar)

  // Opções para o *ngFor do HTML
  alertOptions = [
    { label: '📱 Telegram', value: AlertType.TELEGRAM },
    { label: '📧 E-mail', value: AlertType.EMAIL },
    { label: '💬 SMS', value: AlertType.SMS }
  ];

  // Definição do Formulário
  registerForm = this.fb.group({
    username: ['', Validators.required],
    password: ['', [Validators.required, Validators.minLength(6)]],
    confirmPassword: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]],
    phoneNumber: ['', Validators.required],
    cameraUrl: ['', Validators.required],
    alertTypes: [[AlertType.EMAIL], Validators.required]
  }, { validators: this.passwordMatchValidator });

  // --- GETTERS ---

  get alertTypesArray(): FormControl {
    return this.registerForm.get('alertTypes') as FormControl;
  }

  // Validador personalizado de senha
  passwordMatchValidator(g: FormGroup) {
    return g.get('password')?.value === g.get('confirmPassword')?.value
      ? null : { mismatch: true };
  }

  // --- LÓGICA DOS CHECKBOXES ---

  toggleAlert(value: string, isChecked: boolean) {
    const currentValues = this.alertTypesArray.value as string[];

    if (isChecked) {
      this.alertTypesArray.setValue([...currentValues, value]);
    } else {
      this.alertTypesArray.setValue(currentValues.filter(item => item !== value));
    }
    this.alertTypesArray.markAsTouched();
  }

  // --- SUBMIT ---

  onSubmit() {
    if (this.registerForm.invalid) return;

    this.isLoading.set(true);
    
    // Removemos confirmPassword do payload
    const { confirmPassword, ...registerData } = this.registerForm.getRawValue();

    const finalPayload = {
      ...registerData,
      role: 'USER'
    };

    console.log('Enviando Payload:', finalPayload);

    this.authService.register(finalPayload as any).subscribe({
      next: () => {
        this.isLoading.set(false);
        
        // ✅ SUCESSO:
        // 1. Muda o estado para mostrar a tela de sucesso (troca o HTML)
        this.isRegisterSuccess.set(true);

        // 2. Opcional: Mostra um feedback rápido no topo, mas sem texto no card
        this.snackBar.open('Cadastro realizado com sucesso!', 'OK', { 
          duration: 3000,
          panelClass: ['success-snackbar'] // Use a classe CSS que definimos antes
        });

        // ❌ REMOVIDO: setTimeout e router.navigate automático
      },
      error: (err) => {
        console.error(err);
        this.isLoading.set(false);
        
        // ✅ ERRO:
        // Mostra APENAS no SnackBar (mais limpo que texto vermelho no card)
        this.snackBar.open('Falha ao registrar. Verifique os dados ou tente outro usuário.', 'Fechar', {
          duration: 5000,
          panelClass: ['error-snackbar'] // Use a classe CSS de erro (vermelho)
        });
      }
    });
  }

  // Ação do botão "IR PARA O LOGIN" na tela de sucesso
  goToLogin() {
    this.router.navigate(['/login']);
  }
}