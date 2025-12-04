import { Injectable } from '@angular/core';
import {
  HttpInterceptor,
  HttpRequest,
  HttpHandler,
  HttpErrorResponse
} from '@angular/common/http';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

@Injectable()
export class GlobalErrorInterceptor implements HttpInterceptor {

  constructor(private router: Router) {}

  intercept(req: HttpRequest<any>, next: HttpHandler) {
    return next.handle(req).pipe(
      catchError((error: HttpErrorResponse) => {

        if (error.status === 400) {
          console.error('Bad Request:', error.error?.error || error.message);
          alert(error.error?.error || 'Erro de requisição.');
        }

        if (error.status === 401) {
          console.warn('Unauthorized - redirect to login');
          alert('Usuário ou senha inválidos.');

          // Opcional: limpa token
          localStorage.removeItem('token');

          this.router.navigate(['/login']);
        }

        if (error.status === 500) {
          alert('Erro interno no servidor.');
        }

        return throwError(() => error);
      })
    );
  }
}
