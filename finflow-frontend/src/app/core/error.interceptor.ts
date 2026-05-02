import { HttpErrorResponse, HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { Observable, catchError, throwError } from 'rxjs';
import { AuthService } from './auth.service';
import { SnackbarService } from './snackbar.service';

@Injectable()
export class ErrorInterceptor implements HttpInterceptor {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snackbarService = inject(SnackbarService);

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: HttpErrorResponse) => {
        if (error.status === 401) {
          this.authService.logout(false);
          this.snackbarService.error('Your session expired. Please log in again.');
          void this.router.navigate(['/auth/login']);
        } else if (error.status >= 500) {
          this.snackbarService.error('The server is having trouble right now. Please try again.');
        }

        return throwError(() => error);
      }),
    );
  }
}
