import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
// Importa o SnackBar do Material diretamente
import { MatSnackBar } from '@angular/material/snack-bar';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  // Injeta o componente de notificação do Material
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Ocorreu um erro desconhecido.';

      // Lógica de identificação do erro
	  if (typeof ErrorEvent !== 'undefined' && error.error instanceof ErrorEvent) {
          
          errorMessage = `Erro: ${error.error.message}`;
      } else {
        // Erro do lado do servidor
        switch (error.status) {
          case 0:
            errorMessage = 'Servidor indisponível. Verifique sua conexão.';
            break;
          case 400:
            errorMessage = error.error?.error || 'Dados inválidos.';
            break;
          case 401:
            errorMessage = 'Sessão expirada. Faça login novamente.';
            break;
          case 403:
            errorMessage = 'Acesso negado.';
            break;
          case 404:
            errorMessage = 'Recurso não encontrado.';
            break;
          case 500:
            errorMessage = 'Erro interno no servidor.';
            break;
          default:
            errorMessage = `Erro (${error.status}): ${error.statusText}`;
        }
      }

      // Exibe o SnackBar se não for um erro de sessão (o AuthInterceptor já trata redirects)
      if (error.status !== 401) {
        snackBar.open(errorMessage, 'FECHAR', {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['snackbar-error'] // Usa a classe vermelha definida no styles.scss
        });
      }

      return throwError(() => error);
    })
  );
};
