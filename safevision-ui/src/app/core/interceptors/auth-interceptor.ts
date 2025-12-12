import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  // 1. DEFINIÇÃO DE ROTAS PÚBLICAS (Otimização de Performance)
  // Nessas rotas, NÃO enviamos o token para evitar que o Backend
  // gaste CPU (BCrypt/JWT) tentando validar um token desnecessário.
  const skipTokenUrls = [
    '/auth/login',
    '/auth/register'
    // OBS: '/auth/camera-url' NÃO está aqui, pois ele PRECISA de token.
  ];

  // Verifica se a URL atual faz parte da lista de exceção
  const shouldSkipToken = skipTokenUrls.some(url => req.url.includes(url));

  let token = null;

  // Só tenta ler do localStorage se for navegador (SSR Safety)
  if (isPlatformBrowser(platformId)) {
    token = localStorage.getItem('safevision_token');
  }

  let authReq = req;

  // 2. LÓGICA DE INJEÇÃO DO TOKEN AJUSTADA
  // Só adiciona o header se:
  // a) O token existe
  // b) E a rota NÃO for pública (shouldSkipToken == false)
  if (token && !shouldSkipToken) {
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