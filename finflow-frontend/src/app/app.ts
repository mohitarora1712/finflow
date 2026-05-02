import { AsyncPipe } from '@angular/common';
import { BreakpointObserver, Breakpoints } from '@angular/cdk/layout';
import { Component, inject } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { map, shareReplay } from 'rxjs';
import { AuthService } from './core/auth.service';
import { MATERIAL_IMPORTS } from './shared/material-imports';

interface NavigationItem {
  label: string;
  icon: string;
  route: string;
}

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [AsyncPipe, RouterOutlet, RouterLink, RouterLinkActive, ...MATERIAL_IMPORTS],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  private readonly authService = inject(AuthService);
  private readonly breakpointObserver = inject(BreakpointObserver);

  protected readonly isAuthenticated$ = this.authService.authState$.pipe(map((state) => !!state.token));
  protected readonly userEmail$ = this.authService.authState$.pipe(map((state) => state.email));
  protected readonly navigationItems$ = this.authService.authState$.pipe(
    map((state): NavigationItem[] =>
      state.role === 'ROLE_ADMIN'
        ? [{ label: 'Admin Dashboard', icon: 'shield', route: '/admin/dashboard' }]
        : [{ label: 'User Dashboard', icon: 'account_balance_wallet', route: '/user/dashboard' }],
    ),
  );

  protected readonly isHandset$ = this.breakpointObserver.observe([Breakpoints.Handset]).pipe(
    map((result) => result.matches),
    shareReplay({ bufferSize: 1, refCount: true }),
  );

  protected logout(): void {
    this.authService.logout();
  }

  protected closeDrawerOnHandset(drawer: { close: () => void }): void {
    if (this.breakpointObserver.isMatched(Breakpoints.Handset)) {
      drawer.close();
    }
  }
}
