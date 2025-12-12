import { Injectable, inject } from '@angular/core';
import { MatSnackBar, MatSnackBarConfig, MatSnackBarHorizontalPosition, MatSnackBarVerticalPosition } from '@angular/material/snack-bar';

@Injectable({ providedIn: 'root' })
export class NotificationService {
  private snackBar = inject(MatSnackBar);

 
  showError(message: string) {
    this.openSnackBar(message, 'Fechar', 'error-snackbar', 5000, 'center', 'bottom');
  }


  showSuccess(message: string) {
    this.openSnackBar(message, 'OK', 'success-snackbar', 4000, 'end', 'top');
  }

 
  showInfo(message: string) {
    this.openSnackBar(message, 'OK', 'info-snackbar', 4000, 'end', 'top');
  }

  
  private openSnackBar(
    message: string,
    action: string,
    panelClass: string,
    duration: number,
    horizontalPosition: MatSnackBarHorizontalPosition,
    verticalPosition: MatSnackBarVerticalPosition
  ) {
    const config: MatSnackBarConfig = {
      duration: duration,
      horizontalPosition: horizontalPosition,
      verticalPosition: verticalPosition,
      panelClass: [panelClass] // Adiciona a classe CSS
    };

    this.snackBar.open(message, action, config);
  }
}