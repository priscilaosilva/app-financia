import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ThemeService {
  constructor(private http: HttpClient) {}

  load(): void {
    this.http.get<{ theme_preference: 'LIGHT' | 'DARK' }>(`${environment.apiUrl}/users/me`).subscribe({
      next: (user) => this.apply(user.theme_preference),
      error: () => {}
    });
  }

  toggleTheme(): void {
    const next = document.body.classList.contains('dark-theme') ? 'LIGHT' : 'DARK';
    this.http.put(`${environment.apiUrl}/users/me`, { theme_preference: next }).subscribe({
      next: () => this.apply(next)
    });
  }

  apply(theme: 'LIGHT' | 'DARK'): void {
    document.body.classList.toggle('dark-theme', theme === 'DARK');
  }
}
