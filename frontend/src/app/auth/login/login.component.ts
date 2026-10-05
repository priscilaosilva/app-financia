import { CommonModule } from '@angular/common';
import { Component } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../auth.service';

@Component({
  selector: 'app-login',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './login.component.html'
})
export class LoginComponent {
  mode: 'login' | 'register' = 'login';
  error = '';
  form = this.fb.group({
    name: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]]
  });

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {}

  submit(): void {
    this.error = '';
    const { name, email, password } = this.form.getRawValue();
    if (this.mode === 'register') {
      this.auth.register(name || '', email || '', password || '').subscribe({
        next: () => this.auth.login(email || '', password || '').subscribe({
          next: () => this.router.navigateByUrl('/history'),
          error: () => this.error = 'Conta criada, mas o login falhou.'
        }),
        error: (err) => this.error = err?.error?.error || 'Não foi possível cadastrar.'
      });
      return;
    }
    this.auth.login(email || '', password || '').subscribe({
      next: () => this.router.navigateByUrl('/history'),
      error: (err) => this.error = err?.error?.error || 'Credenciais inválidas.'
    });
  }
}
