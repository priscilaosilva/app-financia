import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from '../auth/auth.service';
import { ThemeService } from '../shared/services/theme.service';

@Component({
  selector: 'app-layout',
  standalone: true,
  imports: [CommonModule, RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app-layout.component.html',
  styles: [`
    .shell { display: grid; grid-template-columns: 240px 1fr; min-height: 100vh; }
    aside { padding: 1.5rem 1rem; border-right: 1px solid var(--line); background: var(--surface); }
    nav { display: flex; flex-direction: column; gap: 0.4rem; margin-top: 1.5rem; }
    a { text-decoration: none; padding: 0.7rem 0.9rem; border-radius: 12px; }
    a.active { background: var(--accent-soft); color: var(--accent); }
  `]
})
export class AppLayoutComponent implements OnInit {
  constructor(public auth: AuthService, private theme: ThemeService) {}

  ngOnInit(): void {
    this.theme.load();
  }
}
