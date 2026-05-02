export interface LoginRequest {
  email: string;
  password: string;
}

export interface SignupRequest {
  email: string;
  password: string;
}

export interface AuthResponse {
  token?: string;
  accessToken?: string;
  jwt?: string;
}

export interface JwtPayload {
  sub?: string;
  email?: string;
  roles?: string[];
  role?: string;
  exp?: number;
}

export interface AuthState {
  token: string | null;
  email: string | null;
  role: 'ROLE_USER' | 'ROLE_ADMIN' | null;
  expiresAt: number | null;
  isDemoMode?: boolean;
}
