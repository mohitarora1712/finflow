import { Component, DestroyRef, inject } from '@angular/core';
import { FormBuilder, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { finalize } from 'rxjs';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { AuthService } from '../../core/auth.service';
import { SnackbarService } from '../../core/snackbar.service';
import { MATERIAL_IMPORTS } from '../../shared/material-imports';

@Component({
  selector: 'app-signup',
  standalone: true,
  imports: [RouterLink, ...MATERIAL_IMPORTS],
  template: `
    <section class="auth-page">
      <mat-card class="auth-card">
        <div class="hero-copy">
          <p class="eyebrow">New workspace</p>
          <h1>Create your FinFlow account</h1>
          <p>Register once, then sign in and start creating draft applications immediately.</p>
        </div>

        <form [formGroup]="signupForm" (ngSubmit)="submit()" class="auth-form">
          <mat-form-field appearance="outline">
            <mat-label>Email</mat-label>
            <input matInput formControlName="email" type="email" autocomplete="email" />
            @if (signupForm.controls.email.touched && signupForm.controls.email.invalid) {
              <mat-error>Enter a valid email address.</mat-error>
            }
          </mat-form-field>

          <mat-form-field appearance="outline">
            <mat-label>Password</mat-label>
            <input matInput formControlName="password" type="password" autocomplete="new-password" />
            @if (signupForm.controls.password.touched && signupForm.controls.password.invalid) {
              <mat-error>Password must be at least 6 characters.</mat-error>
            }
          </mat-form-field>

          <button mat-flat-button type="submit" [disabled]="loading || signupForm.invalid">
            @if (loading) {
              Creating account...
            } @else {
              Sign up
            }
          </button>
        </form>

        <p class="auth-footer">
          Already registered?
          <a routerLink="/auth/login">Go to login</a>
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

      a {
        color: #0d725d;
      }
    `,
  ],
})
export class SignupComponent {
  private readonly fb = inject(FormBuilder);
  private readonly authService = inject(AuthService);
  private readonly snackbarService = inject(SnackbarService);
  private readonly router = inject(Router);
  private readonly destroyRef = inject(DestroyRef);

  protected loading = false;
  protected readonly signupForm = this.fb.nonNullable.group({
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
  });

  protected submit(): void {
    if (this.signupForm.invalid || this.loading) {
      this.signupForm.markAllAsTouched();
      return;
    }

    this.loading = true;

    this.authService
      .signup(this.signupForm.getRawValue())
      .pipe(
        takeUntilDestroyed(this.destroyRef),
        finalize(() => (this.loading = false)),
      )
      .subscribe({
        next: () => {
          this.snackbarService.success('Account created. Please sign in.');
          void this.router.navigate(['/auth/login']);
        },
        error: (error) => {
          this.snackbarService.error(error?.error?.message || 'Signup failed. Please try again.');
        },
      });
  }
}
