import { Injectable, inject } from '@angular/core';
import { ActivatedRouteSnapshot, CanActivate, Router, UrlTree } from '@angular/router';
import { AuthService } from './auth.service';

@Injectable({ providedIn: 'root' })
export class RoleGuard implements CanActivate {
  private readonly authService = inject(AuthService);
  private readonly router = inject(Router);

  canActivate(route: ActivatedRouteSnapshot): boolean | UrlTree {
    const allowedRoles = (route.data['roles'] as string[] | undefined) ?? [];

    if (this.authService.isAuthenticated && this.authService.hasRole(allowedRoles)) {
      return true;
    }

    if (!this.authService.isAuthenticated) {
      return this.router.createUrlTree(['/auth/login']);
    }

    return this.router.createUrlTree([this.authService.getDashboardRoute()]);
  }
}
