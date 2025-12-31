import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { isPlatformBrowser } from '@angular/common';
import { PLATFORM_ID } from '@angular/core';

/**
 * Guard that prevents unauthenticated users from accessing protected routes.
 * Redirects to /login if no valid token is found in storage.
 */
export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const platformId = inject(PLATFORM_ID);

  if (isPlatformBrowser(platformId)) {
    const token = localStorage.getItem('safevision_token');
    if (token) {
      return true;
    }
  }

  // Redirect to login if not authenticated
  router.navigate(['/login']);
  return false;
};