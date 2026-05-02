import { Component, DestroyRef, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/auth.service';
import { SnackbarService } from '../../core/snackbar.service';
import { MATERIAL_IMPORTS } from '../../shared/material-imports';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [RouterLink, ...MATERIAL_IMPORTS],
  template: `
    <section class="auth-page">
      <mat-card class="auth-card">
        <div class="hero-copy">
          <p class="eyebrow">Welcome back</p>
          <h1>Sign in to FinFlow</h1>
          <p>Securely manage applications, approvals, and document uploads from one dashboard.</p>
        </div>

        <form [formGroup]="loginForm" (ngSubmit)="submit()" class="auth-form">
          <mat-form-field appearance="outline">
            <mat-label>Email</mat-label>
            <input matInput formControlName="email" type="email" autocomplete="email" />
            @if (loginForm.controls.email.touched && loginForm.controls.email.invalid) {
              <mat-error>Enter a valid email address.</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Password</mat-label>
            <input matInput formControlName="password" type="password" autocomplete="current-password" />
            @if (loginForm.controls.password.touched && loginForm.controls.password.invalid) {
              <mat-error>Password must be at least 6 characters.</mat-error>
            }
          </mat-form-field>

          <button mat-flat-button type="submit" [disabled]="loading || loginForm.invalid">
            @if (loading) {
              Signing in...
            } @else {
              Login
            }
          </button>
        </form>

        <div class="demo-panel">
          <p class="demo-title">Preview mode</p>
          <p class="demo-copy">Open the app without a backend and explore both dashboards using in-memory demo data.</p>
          <div class="demo-actions">
            <button mat-stroked-button type="button" (click)="enterDemo('ROLE_USER')">Demo User</button>
            <button mat-stroked-button type="button" (click)="enterDemo('ROLE_ADMIN')">Demo Admin</button>
          </div>
        </div>

        <p class="auth-footer">
          New to FinFlow?
          <a routerLink="/auth/signup">Create an account</a>
        </p>
      </mat-card>
    </section>
  `,
  styles: [
    `
      .auth-page {
        min-height: 100vh;
        display: grid;
        place-items: center;
        padding: 1.5rem;
      }

      .auth-card {
        width: min(100%, 520px);
        padding: 1.75rem;
        border-radius: 28px;
        background: rgba(255, 252, 247, 0.96);
        box-shadow: 0 26px 70px rgba(15, 41, 51, 0.12);
      }

      .hero-copy h1 {
        margin: 0.2rem 0 0.55rem;
        font-family: 'Space Grotesk', sans-serif;
        color: #0f2933;
      }

      .hero-copy p,
      .auth-footer {
        color: #566c76;
      }

      .eyebrow {
        margin: 0;
        text-transform: uppercase;
        letter-spacing: 0.08em;
        font-size: 0.85rem;
        color: #0c6b58;
        font-weight: 700;
      }

      .auth-form {
        display: grid;
        gap: 1rem;
        margin: 1.5rem 0 1rem;
      }

      .auth-footer {
        margin: 0;
      }

      .demo-panel {
        margin: 0 0 1rem;
        padding: 1rem;
        border-radius: 18px;
        background: #f4eee4;
      }

      .demo-title,
      .demo-copy {
        margin: 0;
      }

      .demo-title {
        font-weight: 700;
        color: #0f2933;
      }

      .demo-copy {
        margin-top: 0.3rem;
      }

      .demo-actions {
        display: flex;
        gap: 0.75rem;
        margin-top: 0.85rem;
      }

      a {
        color: #0d725d;
      }
    `,
  ],
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly router = inject(Router);
  private readonly route = inject(ActivatedRoute);
  private readonly destroyRef = inject(DestroyRef);

  protected loading = false;
  protected readonly loginForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  constructor() {
    if (this.authService.isAuthenticated) {
      void this.router.navigate([this.authService.getDashboardRoute()]);
    }
  }

  protected submit(): void {
    if (this.loginForm.invalid || this.loading) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.loading = true;
    const credentials = this.loginForm.getRawValue();

    this.authService
      .login(credentials)
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.loading = false)),
      )
      .subscribe({
        next: () => {
          const redirectTo = this.route.snapshot.queryParamMap.get('redirectTo');
          const fallbackRoute = this.authService.getDashboardRoute();
          this.snackbarService.success('Login successful.');
          void this.router.navigateByUrl(redirectTo || fallbackRoute);
        },
        error: (error) => {
          this.snackbarService.error(error?.error?.message || 'Login failed. Please verify your credentials.');
        },
      });
  }

  protected enterDemo(role: 'ROLE_USER' | 'ROLE_ADMIN'): void {
    this.authService
      .loginAsDemo(role)
      .pipe(takeUntilDestroyed(this.destroyRef))
      .subscribe(() => {
        this.snackbarService.info(`Demo ${role === 'ROLE_ADMIN' ? 'admin' : 'user'} session started.`);
        void this.router.navigateByUrl(this.authService.getDashboardRoute(role));
      });
  }
}
