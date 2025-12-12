import { ApplicationConfig, ErrorHandler } from '@angular/core';
import { provideRouter } from '@angular/router';
import { routes } from './app.routes';
import { provideHttpClient, withInterceptors, withFetch } from '@angular/common/http';
import { authInterceptor } from './core/interceptors/auth-interceptor';
import { errorInterceptor } from './core/interceptors/error.interceptor';
import { GlobalErrorHandler } from './core/interceptors/global-error.interceptor';
import { provideEnvironmentNgxMask } from 'ngx-mask';
export const appConfig: ApplicationConfig = {
  providers: [
    // 🔥 IMPORTANTE: sem isso, nenhuma rota funciona!
    provideRouter(routes),

    // Interceptores HTTP
    provideHttpClient(withInterceptors([authInterceptor, errorInterceptor]), withFetch()),
    provideEnvironmentNgxMask(),
    // Handler global de erros
    { provide: ErrorHandler, useClass: GlobalErrorHandler }
  ]
};
