import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID); // <--- Injeção do Platform ID

  let token = null;

  // CORREÇÃO: Só tenta ler do localStorage se for navegador
  if (isPlatformBrowser(platformId)) {
    token = localStorage.getItem('safevision_token');
  }

  let authReq = req;
  if (token) {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(authReq).pipe(
    catchError(error => {
      if (error.status === 401) {
        // Só tenta limpar storage se for navegador
        if (isPlatformBrowser(platformId)) {
            localStorage.removeItem('safevision_token');
        }
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};
