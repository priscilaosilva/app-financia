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
  mode: 'login' | 'register' | 'forgot' = 'login';
  error = '';
  info = '';
  attempted = false;
  form = this.fb.group({
    name: [''],
    email: ['', [Validators.required, Validators.email]],
    password: ['', [Validators.required, Validators.minLength(6)]],
    passwordConfirmation: ['']
  });

  constructor(private fb: FormBuilder, private auth: AuthService, private router: Router) {}

  setMode(mode: 'login' | 'register' | 'forgot'): void {
    this.mode = mode;
    this.error = '';
    this.info = '';
    this.form.controls.passwordConfirmation.setValue('');
    this.form.controls.password.setValidators([Validators.required, Validators.minLength(6)]);
    this.form.controls.password.updateValueAndValidity();
  }

  submit(): void {
    this.attempted = true;
    this.error = '';
    this.info = '';
    const emailControl = this.form.controls.email;
    const trimmed = (emailControl.value || '').trim();
    emailControl.setValue(trimmed, { emitEvent: false });
    if (emailControl.invalid) {
      this.error = 'Informe um e-mail no formato nome@dominio.';
      return;
    }
    if (this.form.invalid) {
      return;
    }
    const { name, email, password, passwordConfirmation } = this.form.getRawValue();
    if (this.mode === 'forgot') {
      if ((password || '') !== (passwordConfirmation || '')) {
        this.error = 'A confirmação deve ser igual à senha nova.';
        return;
      }
      this.auth.forgotPassword(email || '', password || '', passwordConfirmation || '').subscribe({
        next: () => {
          this.setMode('login');
          this.info = 'Se houver conta com este e-mail, a senha foi atualizada. Entre com a senha nova.';
        },
        error: (err) => this.error = err?.error?.error || 'Não foi possível atualizar a senha.'
      });
      return;
    }
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
