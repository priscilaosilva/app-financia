import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-recurrence-scope-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="overlay" *ngIf="open">
      <div class="card" style="max-width: 420px; margin: 15vh auto;">
        <h3>Esta é uma transação recorrente</h3>
        <p class="muted">A alteração deve valer para:</p>
        <button class="btn ghost" (click)="choose('THIS_ONLY')">Somente esta ocorrência</button>
        <button class="btn ghost" (click)="choose('THIS_AND_FUTURE')">Esta e todas as futuras</button>
        <button class="btn ghost" (click)="choose('ALL')">Todas as ocorrências</button>
        <button class="btn" (click)="close.emit()">Cancelar</button>
      </div>
    </div>
  `,
  styles: [`
    .overlay { position: fixed; inset: 0; background: rgba(0,0,0,.35); padding: 1rem; z-index: 20; }
    button { display: block; width: 100%; margin-bottom: 0.6rem; }
  `]
})
export class RecurrenceScopeDialogComponent {
  @Input() open = false;
  @Output() selected = new EventEmitter<string>();
  @Output() close = new EventEmitter<void>();

  choose(scope: string): void {
    this.selected.emit(scope);
  }
}
