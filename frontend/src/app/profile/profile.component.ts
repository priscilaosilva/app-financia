import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { HttpClient } from '@angular/common/http';
import { AuthService } from '../auth/auth.service';
import { environment } from '../../environments/environment';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './profile.component.html'
})
export class ProfileComponent implements OnInit {
  message = '';
  profile = this.fb.group({
    name: ['', Validators.required],
    email: ['', [Validators.required, Validators.email]]
  });
  password = this.fb.group({
    current_password: ['', Validators.required],
    new_password: ['', [Validators.required, Validators.minLength(6)]]
  });

  constructor(private fb: FormBuilder, private http: HttpClient, private auth: AuthService) {}

  ngOnInit(): void {
    this.auth.getCurrentUser().subscribe((user) => this.profile.patchValue(user));
  }

  saveProfile(): void {
    this.http.put(`${environment.apiUrl}/users/me`, this.profile.getRawValue()).subscribe({
      next: () => this.message = 'Perfil atualizado.',
      error: (err) => this.message = err?.error?.error || 'Erro ao atualizar perfil.'
    });
  }

  changePassword(): void {
    this.http.put(`${environment.apiUrl}/users/me/password`, this.password.getRawValue()).subscribe({
      next: () => this.message = 'Senha alterada.',
      error: (err) => this.message = err?.error?.error || 'Erro ao alterar senha.'
    });
  }

  deleteAccount(): void {
    if (!confirm('Excluir a conta e todos os dados?')) {
      return;
    }
    this.http.delete(`${environment.apiUrl}/users/me`).subscribe(() => this.auth.logout());
  }
}
