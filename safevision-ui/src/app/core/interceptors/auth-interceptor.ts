import { HttpInterceptorFn } from '@angular/common/http';
import { inject, PLATFORM_ID } from '@angular/core';
import { isPlatformBrowser } from '@angular/common';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';

/**
 * Authentication Interceptor.
 * Responsibilities:
 * 1. Automatically injects the JWT Bearer token into requests.
 * 2. Excludes public routes from token injection to optimize performance.
 * 3. Handles 401 Unauthorized responses by clearing storage and redirecting to login.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  // Define routes that should not include the Authorization header
  const skipTokenUrls = [
    '/auth/login',
    '/auth/register'
  ];

  const shouldSkipToken = skipTokenUrls.some(url => req.url.includes(url));
  let token: string | null = null;

  if (isPlatformBrowser(platformId)) {
    token = localStorage.getItem('safevision_token');
  }

  let authReq = req;

  // Inject token if available and route is not public
  if (token && !shouldSkipToken) {
    authReq = req.clone({
      setHeaders: { Authorization: `Bearer ${token}` }
    });
  }

  return next(authReq).pipe(
    catchError(error => {
      if (error.status === 401) {
        if (isPlatformBrowser(platformId)) {
          localStorage.removeItem('safevision_token');
        }
        router.navigate(['/login']);
      }
      return throwError(() => error);
    })
  );
};