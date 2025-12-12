import { ErrorHandler, Injectable, NgZone, inject } from '@angular/core';
// Importa diretamente o SnackBar do Material
import { MatSnackBar } from '@angular/material/snack-bar';

@Injectable()
export class GlobalErrorHandler implements ErrorHandler {
  // Injeção direta do componente visual
  private snackBar = inject(MatSnackBar);
  private zone = inject(NgZone);

  handleError(error: any): void {
    // 1. Loga o erro real no console para o programador
    console.error('🔥 CRITICAL APP ERROR:', error);

    // 2. Tenta extrair uma mensagem legível, se existir
    const message = error?.message || 'Ocorreu um erro inesperado.';

    // 3. Executa a abertura do SnackBar dentro da NgZone
    // Isso garante que a UI atualize mesmo se o erro vier de um evento assíncrono externo
    this.zone.run(() => {
      this.snackBar.open(
        `Erro de Aplicação: ${message}`, // Mensagem para o utilizador
        'FECHAR',
        {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['snackbar-error'] // Usa a mesma classe vermelha do styles.scss
        }
      );
    });

    // TODO: Integração com Sentry/LogRocket seria feita aqui
  }
}
