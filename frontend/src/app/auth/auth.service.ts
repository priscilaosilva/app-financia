import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenKey = 'financia_token';

  constructor(private http: HttpClient, private router: Router) {}

  login(email: string, password: string) {
    return this.http.post<{ token: string; expires_at: string }>(`${environment.apiUrl}/auth/login`, { email, password }).pipe(
      tap((res) => localStorage.setItem(this.tokenKey, res.token))
    );
  }

  register(name: string, email: string, password: string) {
    return this.http.post(`${environment.apiUrl}/auth/register`, { name, email, password });
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
    this.router.navigateByUrl('/login');
  }

  isAuthenticated(): boolean {
    return !!this.getToken();
  }

  getToken(): string | null {
    return localStorage.getItem(this.tokenKey);
  }

  getCurrentUser() {
    return this.http.get<{ id: number; name: string; email: string; theme_preference: 'LIGHT' | 'DARK' }>(
      `${environment.apiUrl}/users/me`
    );
  }
}
