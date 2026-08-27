import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface SignupPayload {
  name: string;
  email: string;
  password: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: string;
  expiresIn: number;
}

function getApiBaseUrl(): string {
  if (typeof window !== 'undefined') {
    // Se estiver rodando na VPS na porta 4201, aponta para a porta 8087 da API, senão 8080
    const port = window.location.port === '4201' ? '8087' : '8080';
    return `${window.location.protocol}//${window.location.hostname}:${port}`;
  }
  return 'http://localhost:8080';
}

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/auth';

  signup(payload: SignupPayload): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(`${this.baseUrl}/signup`, payload);
  }
}
