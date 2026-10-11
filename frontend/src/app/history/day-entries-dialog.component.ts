import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { HistoryEntry } from './history.service';

@Component({
  selector: 'app-day-entries-dialog',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="overlay" *ngIf="open">
      <div class="card" style="max-width: 480px; margin: 10vh auto;">
        <h3>Lançamentos de {{ date }}</h3>
        <p class="muted" *ngIf="!entries.length">Não há registros para apagar neste dia.</p>
        <label class="field" *ngFor="let entry of entries" style="flex-direction: row; align-items: center; gap: 0.6rem;">
          <input type="checkbox" [checked]="isSelected(entry)" (change)="toggle(entry)">
          <span>{{ entry.type === 'INCOME' ? 'Entrada' : 'Saída' }} · {{ entry.label }} · {{ entry.amount | number:'1.2-2' }}</span>
        </label>
        <p class="error" *ngIf="error">{{ error }}</p>
        <div style="display:flex; gap: 0.6rem; margin-top: 1rem;">
          <button class="btn danger" (click)="askDelete()">Apagar</button>
          <button class="btn ghost" (click)="close.emit()">Fechar</button>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .overlay { position: fixed; inset: 0; background: rgba(0,0,0,.35); padding: 1rem; z-index: 20; }
  `]
})
export class DayEntriesDialogComponent {
  @Input() open = false;
  @Input() date = '';
  @Input() entries: HistoryEntry[] = [];
  @Output() close = new EventEmitter<void>();
  @Output() deleteRequested = new EventEmitter<HistoryEntry[]>();

  selected = new Set<string>();
  error = '';

  key(entry: HistoryEntry): string {
    return `${entry.type}-${entry.id}`;
  }

  isSelected(entry: HistoryEntry): boolean {
    return this.selected.has(this.key(entry));
  }

  toggle(entry: HistoryEntry): void {
    const id = this.key(entry);
    if (this.selected.has(id)) {
      this.selected.delete(id);
    } else {
      this.selected.add(id);
    }
    this.error = '';
  }

  askDelete(): void {
    const chosen = this.entries.filter((entry) => this.isSelected(entry));
    if (!chosen.length) {
      this.error = 'Selecione ao menos um registro.';
      return;
    }
    this.error = '';
    this.deleteRequested.emit(chosen);
  }

  resetSelection(entries: HistoryEntry[]): void {
    this.entries = entries;
    const valid = new Set(entries.map((entry) => this.key(entry)));
    [...this.selected].forEach((id) => {
      if (!valid.has(id)) {
        this.selected.delete(id);
      }
    });
  }
}
