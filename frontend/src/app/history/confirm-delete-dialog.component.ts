import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

@Component({
  selector: 'app-confirm-delete-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="overlay" *ngIf="open">
      <div class="card" style="max-width: 420px; margin: 18vh auto;">
        <h3>Tem certeza?</h3>
        <p class="muted">Os lançamentos selecionados serão excluídos.</p>
        <div style="display:flex; gap: 0.6rem; margin-top: 1rem;">
          <button class="btn danger" (click)="confirm.emit()">Excluir</button>
          <button class="btn ghost" (click)="cancel.emit()">Cancelar</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .overlay { position: fixed; inset: 0; background: rgba(0,0,0,.45); padding: 1rem; z-index: 25; }
  `]
})
export class ConfirmDeleteDialogComponent {
  @Input() open = false;
  @Output() confirm = new EventEmitter<void>();
  @Output() cancel = new EventEmitter<void>();
}
