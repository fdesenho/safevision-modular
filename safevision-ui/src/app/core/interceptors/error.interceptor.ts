import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { AuthService } from '../services/auth.service';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  // Opcional: Injetar AuthService se precisar fazer logout no 401 aqui dentro
  // const authService = inject(AuthService);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'Ocorreu um erro inesperado. Tente novamente.';

      if (error.error instanceof ErrorEvent) {
        // Erro do lado do cliente (ex: rede)
        errorMessage = `Erro de conexão: ${error.error.message}`;
      } else {
        // Erro do lado do servidor (Spring Boot)
        switch (error.status) {
          case 0:
            errorMessage = 'Servidor indisponível. Verifique sua conexão ou se o Docker está rodando.';
            break;

          case 400: // Bad Request (Validações do Java)
            // Tenta pegar a mensagem { "error": "..." } ou { "message": "..." }
            errorMessage = error.error?.error || error.error?.message || 'Dados inválidos.';
            break;

          case 401: // Unauthorized
            errorMessage = 'Sessão expirada ou credenciais inválidas.';
             localStorage.removeItem('safevision_token');
             router.navigate(['/login']);
            break;

          case 403: // Forbidden
            errorMessage = 'Você não tem permissão para realizar esta ação.';
            break;

          case 404: // Not Found
            errorMessage = 'Recurso não encontrado no servidor.';
            break;

          case 500: // Internal Server Error
            errorMessage = 'Erro interno no servidor. Contate o suporte.';
            break;

          default:
             // Tenta usar a mensagem crua se houver
            errorMessage = error.error?.message || `Erro código ${error.status}`;
        }
      }

      // Log para o desenvolvedor
      console.error('❌ [Error Interceptor]', error);

      // Retorna o erro modificado (apenas a string) para o componente exibir fácil
      return throwError(() => new Error(errorMessage));
    })
  );
};
