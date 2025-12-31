import { HttpErrorResponse, HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { catchError, throwError } from 'rxjs';
import { MatSnackBar } from '@angular/material/snack-bar';

/**
 * Global HTTP Error Interceptor.
 * Captures all failed requests and displays a Material SnackBar notification.
 * Handles specific status codes to provide contextual feedback.
 */
export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const snackBar = inject(MatSnackBar);

  return next(req).pipe(
    catchError((error: HttpErrorResponse) => {
      let errorMessage = 'An unknown error occurred.';

      if (error.error instanceof ErrorEvent) {
        // Client-side or network error
        errorMessage = `Client Error: ${error.error.message}`;
      } else {
        // Server-side error codes logic
        switch (error.status) {
          case 0:
            errorMessage = 'Server is unreachable. Please check your connection.';
            break;
          case 400:
            errorMessage = error.error?.message || 'Invalid data provided.';
            break;
          case 401:
            // 401 is handled by AuthInterceptor for redirects, 
            // but we define the message here for consistency.
            errorMessage = 'Session expired. Please login again.';
            break;
          case 403:
            errorMessage = 'Access denied. You do not have permission.';
            break;
          case 404:
            errorMessage = 'The requested resource was not found.';
            break;
          case 500:
            errorMessage = 'Internal server error. Please try again later.';
            break;
          default:
            errorMessage = `Error (${error.status}): ${error.statusText}`;
        }
      }

      // Display notification if it's not a session expiration (handled by AuthInterceptor)
      if (error.status !== 401) {
        snackBar.open(errorMessage, 'CLOSE', {
          duration: 5000,
          horizontalPosition: 'right',
          verticalPosition: 'top',
          panelClass: ['snackbar-error']
        });
      }

      return throwError(() => error);
    })
  );
};