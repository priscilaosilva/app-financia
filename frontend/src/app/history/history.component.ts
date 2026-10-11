import { CommonModule } from '@angular/common';
import { Component, OnInit, ViewChild } from '@angular/core';
import { HistoryEntry, HistoryResponse, HistoryRow, HistoryService } from './history.service';
import { IncomeFormComponent } from '../income/income-form/income-form.component';
import { ExpenseFormComponent } from '../expense/expense-form/expense-form.component';
import { RecurrenceScopeDialogComponent } from '../shared/components/recurrence-scope-dialog/recurrence-scope-dialog.component';
import { DayEntriesDialogComponent } from './day-entries-dialog.component';
import { ConfirmDeleteDialogComponent } from './confirm-delete-dialog.component';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [
    CommonModule,
    IncomeFormComponent,
    ExpenseFormComponent,
    RecurrenceScopeDialogComponent,
    DayEntriesDialogComponent,
    ConfirmDeleteDialogComponent
  ],
  templateUrl: './history.component.html'
})
export class HistoryComponent implements OnInit {
  @ViewChild(DayEntriesDialogComponent) dayDialog?: DayEntriesDialogComponent;

  month = new Date();
  history?: HistoryResponse;
  balance = 0;
  showIncome = false;
  showExpense = false;
  listOpen = false;
  confirmOpen = false;
  scopeOpen = false;
  activeDate = '';
  dayEntries: HistoryEntry[] = [];
  pendingItems: HistoryEntry[] = [];

  constructor(private historyApi: HistoryService) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(keepList = false): void {
    const from = this.firstDay();
    const to = this.lastDay();
    this.historyApi.getHistory(from, to).subscribe((data) => {
      this.history = data;
      if (keepList && this.listOpen) {
        const row = data.rows.find((item) => item.date === this.activeDate);
        this.dayEntries = this.deletable(row);
        this.dayDialog?.resetSelection(this.dayEntries);
        if (!this.dayEntries.length) {
          this.listOpen = false;
        }
      }
    });
    this.historyApi.getBalance().subscribe((data) => this.balance = data.balance);
  }

  previous(): void {
    this.month = new Date(this.month.getFullYear(), this.month.getMonth() - 1, 1);
    this.reload();
  }

  next(): void {
    this.month = new Date(this.month.getFullYear(), this.month.getMonth() + 1, 1);
    this.reload();
  }

  isEmpty(row: HistoryRow): boolean {
    return row.has_movement === false || !row.entries?.length;
  }

  hasDeletable(row: HistoryRow): boolean {
    return this.deletable(row).length > 0;
  }

  openManage(row: HistoryRow): void {
    this.activeDate = row.date;
    this.dayEntries = this.deletable(row);
    if (!this.dayEntries.length) {
      return;
    }
    this.listOpen = true;
  }

  onDeleteRequested(items: HistoryEntry[]): void {
    this.pendingItems = items;
    this.confirmOpen = true;
  }

  onConfirmCancel(): void {
    this.confirmOpen = false;
    this.pendingItems = [];
  }

  onConfirmDelete(): void {
    this.confirmOpen = false;
    const needsScope = this.pendingItems.some((item) => item.is_recurring_projection || item.is_recurring);
    if (needsScope) {
      this.scopeOpen = true;
      return;
    }
    this.submitDelete();
  }

  onScope(scope: string): void {
    this.scopeOpen = false;
    this.submitDelete(scope);
  }

  onScopeClose(): void {
    this.scopeOpen = false;
    this.pendingItems = [];
  }

  closeList(): void {
    this.listOpen = false;
    this.pendingItems = [];
  }

  private submitDelete(scope?: string): void {
    const items = this.pendingItems
      .filter((item) => item.id != null)
      .map((item) => ({ id: item.id as number, type: item.type }));
    this.historyApi.deleteDayEntries(this.activeDate, items, scope).subscribe({
      next: () => {
        this.pendingItems = [];
        this.reload(true);
      },
      error: () => {
        this.pendingItems = [];
      }
    });
  }

  private deletable(row?: HistoryRow): HistoryEntry[] {
    return (row?.entries ?? []).filter((entry) =>
      entry.id != null && (entry.type === 'INCOME' || entry.type === 'EXPENSE')
    );
  }

  private firstDay(): string {
    const year = this.month.getFullYear();
    const month = this.month.getMonth();
    return `${year}-${String(month + 1).padStart(2, '0')}-01`;
  }

  private lastDay(): string {
    const year = this.month.getFullYear();
    const month = this.month.getMonth();
    const last = new Date(year, month + 1, 0).getDate();
    return `${year}-${String(month + 1).padStart(2, '0')}-${String(last).padStart(2, '0')}`;
  }

  monthLabel(): string {
    return this.month.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  }
}
