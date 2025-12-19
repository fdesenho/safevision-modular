import { Component, inject, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators, FormControl } from '@angular/forms';
import { MatDialogRef, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatIconModule } from '@angular/material/icon';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatChipsModule } from '@angular/material/chips';

// Imports do Projeto
import { AuthService } from '../../core/services/auth.service';
import { AlertType, UserUpdateRequest } from '../../core/models/app.models';
import { PhoneMaskDirective } from '../../shared/directives/phone-mask.directive';



@Component({
  selector: 'app-user-profile-dialog',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatDialogModule,
    MatButtonModule,
    MatFormFieldModule,
    MatInputModule,
    MatIconModule,
    MatChipsModule,
    MatProgressBarModule,
    PhoneMaskDirective // 📍 Adicione aqui
  ],
  templateUrl: './user-profile-dialog.component.html',
  styleUrls: ['./user-profile-dialog.component.scss']
})
export class UserProfileDialogComponent implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private snackBar = inject(MatSnackBar);
  public dialogRef = inject(MatDialogRef<UserProfileDialogComponent>);

  profileForm: FormGroup;
  isLoading = false;
  username: string = '';

  alertOptions = [
    { label: 'Telegram', value: AlertType.TELEGRAM, icon: 'send' },
    { label: 'E-mail', value: AlertType.EMAIL, icon: 'email' },
    { label: 'SMS', value: AlertType.SMS, icon: 'sms' }
  ];

  constructor() {
    this.profileForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]], // 📍 Obrigatório
      phoneNumber: ['', [Validators.required]],             // 📍 Obrigatório
      telegramChatId: [''],
      cameraConnectionUrl: [''],

      // Senha e Confirmação
      password: ['', [Validators.minLength(6)]],
      confirmPassword: [''],

      alertPreferences: [[]]
    }, { validators: this.passwordMatchValidator }); // 📍 Validador de grupo
  }

  get alertPreferencesControl(): FormControl {
    return this.profileForm.get('alertPreferences') as FormControl;
  }

  // 📍 Lógica de Validação de Senha
  // Só exige confirmação se o campo 'password' estiver preenchido
  passwordMatchValidator(g: FormGroup) {
    const pass = g.get('password')?.value;
    const confirm = g.get('confirmPassword')?.value;

    // Se senha estiver vazia, não valida nada (usuário não quer mudar)
    if (!pass) return null;

    // Se senha tem valor, confirmação deve ser igual
    return pass === confirm ? null : { mismatch: true };
  }

  ngOnInit(): void {
    const currentUser = this.authService.currentUser();
    if (currentUser) {
      this.username = currentUser.username;
      this.loadFullUserData(this.username);
    }
  }

  toggleAlert(value: AlertType, isChecked: boolean) {
    const currentValues = this.alertPreferencesControl.value as AlertType[];
    if (isChecked) {
      if (!currentValues.includes(value)) {
        this.alertPreferencesControl.setValue([...currentValues, value]);
      }
    } else {
      this.alertPreferencesControl.setValue(currentValues.filter(item => item !== value));
    }
    this.alertPreferencesControl.markAsTouched();
  }

  loadFullUserData(username: string) {
    this.isLoading = true;
    this.profileForm.disable();

    this.authService.getUserProfile(username).subscribe({
      next: (profile) => {
        this.profileForm.patchValue({
          email: profile.email,
          phoneNumber: profile.phoneNumber,
          cameraConnectionUrl: profile.cameraConnectionUrl,
          alertPreferences: profile.alertPreferences || []
        });
        this.isLoading = false;
        this.profileForm.enable();
      },
      error: (err) => {
        console.error(err);
        this.snackBar.open('Erro ao carregar dados do perfil.', 'Fechar');
        this.isLoading = false;
        this.profileForm.enable();
      }
    });
  }

  onSubmit() {
    if (this.profileForm.invalid) return;

    this.isLoading = true;
    this.profileForm.disable();

    // 📍 Remove confirmPassword e telegramChatId (se backend não suportar ainda)
    // Extraímos apenas o que o DTO UserUpdateRequest espera
    const { confirmPassword, telegramChatId, ...formData } = this.profileForm.value;

    const updateData: UserUpdateRequest = {
      email: formData.email,
      phoneNumber: formData.phoneNumber,
      cameraConnectionUrl: formData.cameraConnectionUrl,
      password: formData.password || null,
      alertPreferences: formData.alertPreferences
    };

    this.authService.updateUser(updateData).subscribe({
      next: () => {
        this.snackBar.open('Perfil atualizado com sucesso!', 'OK', { duration: 3000 });
        this.dialogRef.close(true);
      },
      error: () => {
        
        this.isLoading = false;
        this.profileForm.enable();
      }
    });
  }
}
