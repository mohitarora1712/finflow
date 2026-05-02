import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Router } from '@angular/router';
import { BehaviorSubject, Observable, map, of, tap } from 'rxjs';
import { API_BASE_URL } from './constants/api.constants';
import { AuthResponse, AuthState, JwtPayload, LoginRequest, SignupRequest } from './models/auth.models';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);
  private readonly storageKey = 'finflow.jwt';
  private readonly demoStorageKey = 'finflow.demo-session';
  private readonly authStateSubject = new BehaviorSubject<AuthState>({
    token: null,
    email: null,
    role: null,
    expiresAt: null,
    isDemoMode: false,
  });

  readonly authState$ = this.authStateSubject.asObservable();

  constructor() {
    this.hydrateFromStorage();
  }

  get token(): string | null {
    return this.authStateSubject.value.token;
  }

  get role(): AuthState['role'] {
    return this.authStateSubject.value.role;
  }

  get isAuthenticated(): boolean {
    const { token, expiresAt } = this.authStateSubject.value;
    return !!token && !!expiresAt && Date.now() < expiresAt;
  }

  login(payload: LoginRequest): Observable<void> {
    return this.http.post<AuthResponse>(`${API_BASE_URL}/auth/login`, payload).pipe(
      tap((response) => this.persistSession(this.extractToken(response), payload.email)),
      map(() => void 0),
    );
  }

  signup(payload: SignupRequest): Observable<void> {
    return this.http.post<void>(`${API_BASE_URL}/auth/signup`, payload).pipe(map(() => void 0));
  }

  loginAsDemo(role: 'ROLE_USER' | 'ROLE_ADMIN'): Observable<void> {
    const email = role === 'ROLE_ADMIN' ? 'demo.admin@finflow.dev' : 'demo.user@finflow.dev';
    const token = this.createDemoToken(email, role);
    this.persistSession(token, email, true);
    return of(void 0);
  }

  logout(redirect = true): void {
    localStorage.removeItem(this.storageKey);
    localStorage.removeItem(this.demoStorageKey);
    this.authStateSubject.next({
      token: null,
      email: null,
      role: null,
      expiresAt: null,
      isDemoMode: false,
    });

    if (redirect) {
      void this.router.navigate(['/auth/login']);
    }
  }

  hasRole(roles: string[]): boolean {
    const role = this.authStateSubject.value.role;
    return !!role && roles.includes(role);
  }

  getDashboardRoute(role = this.authStateSubject.value.role): string {
    return role === 'ROLE_ADMIN' ? '/admin/dashboard' : '/user/dashboard';
  }

  get isDemoMode(): boolean {
    return !!this.authStateSubject.value.isDemoMode;
  }

  private hydrateFromStorage(): void {
    const demoToken = localStorage.getItem(this.demoStorageKey);
    if (demoToken) {
      const payload = this.decodeToken(demoToken);
      if (payload?.exp && Date.now() < payload.exp * 1000) {
        this.authStateSubject.next({
          token: demoToken,
          email: payload.email ?? payload.sub ?? null,
          role: this.resolveRole(payload),
          expiresAt: payload.exp * 1000,
          isDemoMode: true,
        });
        return;
      }
      localStorage.removeItem(this.demoStorageKey);
    }

    const token = localStorage.getItem(this.storageKey);

    if (!token) {
      return;
    }

    const payload = this.decodeToken(token);
    if (!payload?.exp || Date.now() >= payload.exp * 1000) {
      this.logout(false);
      return;
    }

    this.authStateSubject.next({
      token,
      email: payload.email ?? payload.sub ?? null,
      role: this.resolveRole(payload),
      expiresAt: payload.exp * 1000,
      isDemoMode: false,
    });
  }

  private persistSession(token: string, fallbackEmail: string, isDemoMode = false): void {
    const payload = this.decodeToken(token);

    localStorage.setItem(isDemoMode ? this.demoStorageKey : this.storageKey, token);
    if (isDemoMode) {
      localStorage.removeItem(this.storageKey);
    } else {
      localStorage.removeItem(this.demoStorageKey);
    }
    this.authStateSubject.next({
      token,
      email: payload?.email ?? payload?.sub ?? fallbackEmail,
      role: this.resolveRole(payload),
      expiresAt: payload?.exp ? payload.exp * 1000 : null,
      isDemoMode,
    });
  }

  private extractToken(response: AuthResponse): string {
    const token = response.token ?? response.accessToken ?? response.jwt;

    if (!token) {
      throw new Error('Token missing in login response.');
    }

    return token;
  }

  private resolveRole(payload: JwtPayload | null): AuthState['role'] {
    const roleCandidate = payload?.roles?.[0] ?? payload?.role ?? null;
    return roleCandidate === 'ROLE_ADMIN' || roleCandidate === 'ROLE_USER' ? roleCandidate : null;
  }

  private decodeToken(token: string): JwtPayload | null {
    try {
      const [, encodedPayload] = token.split('.');
      if (!encodedPayload) {
        return null;
      }

      const normalized = encodedPayload.replace(/-/g, '+').replace(/_/g, '/');
      const padded = normalized.padEnd(normalized.length + ((4 - (normalized.length % 4)) % 4), '=');
      return JSON.parse(atob(padded)) as JwtPayload;
    } catch {
      return null;
    }
  }

  private createDemoToken(email: string, role: 'ROLE_USER' | 'ROLE_ADMIN'): string {
    const header = this.toBase64Url({ alg: 'none', typ: 'JWT' });
    const payload = this.toBase64Url({
      sub: email,
      email,
      roles: [role],
      exp: Math.floor(Date.now() / 1000) + 60 * 60 * 12,
    });

    return `${header}.${payload}.demo-signature`;
  }

  private toBase64Url(value: object): string {
    return btoa(JSON.stringify(value)).replace(/\+/g, '-').replace(/\//g, '_').replace(/=+$/g, '');
  }
}
