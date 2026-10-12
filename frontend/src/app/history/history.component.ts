import { CommonModule } from '@angular/common';
import { Component, HostListener, OnInit, ViewChild } from '@angular/core';
import { switchMap } from 'rxjs/operators';
import { HistoryEntry, HistoryResponse, HistoryRow, HistoryService } from './history.service';
import { IncomeFormComponent, IncomeSeed } from '../income/income-form/income-form.component';
import { ExpenseFormComponent, ExpenseSeed } from '../expense/expense-form/expense-form.component';
import { RecurrenceScopeDialogComponent } from '../shared/components/recurrence-scope-dialog/recurrence-scope-dialog.component';
import { DayEntriesDialogComponent } from './day-entries-dialog.component';
import { ConfirmDeleteDialogComponent } from './confirm-delete-dialog.component';
import { IncomeService } from '../income/income.service';
import { ExpenseService } from '../expense/expense.service';

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
  templateUrl: './history.component.html',
  styles: [`
    .entry-overlay {
      position: fixed;
      inset: 0;
      background: rgba(0, 0, 0, .35);
      padding: 1rem;
      z-index: 30;
      overflow: auto;
    }
    .entry-overlay .card {
      max-width: 520px;
      margin: 8vh auto;
      max-height: 90dvh;
      overflow: auto;
    }
    .row-actions {
      display: flex;
      flex-wrap: wrap;
      gap: 0.35rem;
      justify-content: flex-end;
    }
    .row-actions .btn {
      padding: 0.2rem 0.45rem;
      font-size: 0.8rem;
    }
    .type-switch { display: flex; gap: 0.5rem; margin-bottom: 1rem; }
    .sheet-wrap { overflow-x: auto; }
  `]
})
export class HistoryComponent implements OnInit {
  @ViewChild(DayEntriesDialogComponent) dayDialog?: DayEntriesDialogComponent;
  @ViewChild(IncomeFormComponent) incomeForm?: IncomeFormComponent;
  @ViewChild(ExpenseFormComponent) expenseForm?: ExpenseFormComponent;

  month = new Date();
  history?: HistoryResponse;
  balance = 0;
  entryModal: 'income' | 'expense' | null = null;
  entryMode: 'create' | 'edit' = 'create';
  entryInitialDate = '';
  entrySeed: IncomeSeed & ExpenseSeed = {};
  editOriginalType: 'INCOME' | 'EXPENSE' | null = null;
  editOriginalId: number | null = null;
  editIsRecurring = false;
  pendingEdit: { date: string; payload: Record<string, unknown> } | null = null;
  scopeMode: 'delete' | 'edit' = 'delete';
  listOpen = false;
  confirmOpen = false;
  scopeOpen = false;
  activeDate = '';
  dayEntries: HistoryEntry[] = [];
  pendingItems: HistoryEntry[] = [];

  constructor(
    private historyApi: HistoryService,
    private incomeApi: IncomeService,
    private expenseApi: ExpenseService
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  @HostListener('document:keydown', ['$event'])
  onDocumentKeydown(event: KeyboardEvent): void {
    if (event.key !== 'Escape') {
      return;
    }
    if (this.scopeOpen || this.confirmOpen) {
      return;
    }
    if (this.entryModal) {
      this.closeEntryModal();
    }
  }

  openEntryFromTop(kind: 'income' | 'expense'): void {
    this.entryMode = 'create';
    this.editOriginalId = null;
    this.openEntry(kind, '');
  }

  openEntryFromRow(kind: 'income' | 'expense', row: HistoryRow): void {
    this.entryMode = 'create';
    this.editOriginalId = null;
    this.openEntry(kind, row.date);
  }

  closeEntryModal(): void {
    this.entryModal = null;
    this.entryInitialDate = '';
    this.entrySeed = {};
    this.entryMode = 'create';
    this.editOriginalId = null;
    this.editOriginalType = null;
    this.pendingEdit = null;
  }

  onEntrySaved(date: string): void {
    this.closeEntryModal();
    this.goToMonthOf(date);
    this.reload(this.listOpen);
  }

  onEditSubmitted(event: { date: string; payload: Record<string, unknown> }): void {
    this.pendingEdit = event;
    if (this.editIsRecurring) {
      this.scopeMode = 'edit';
      this.scopeOpen = true;
      return;
    }
    this.commitEdit();
  }

  switchEditKind(kind: 'income' | 'expense'): void {
    if (kind === this.entryModal) {
      return;
    }
    const snap = this.entryModal === 'income'
      ? this.incomeForm?.snapshot()
      : this.expenseForm?.snapshot();
    this.entrySeed = {
      date: snap?.date || this.entrySeed.date,
      amount: snap?.amount ?? this.entrySeed.amount,
      description: snap?.description ?? this.entrySeed.description,
      repeat: snap?.repeat,
      frequency: snap?.frequency
    };
    this.entryInitialDate = this.entrySeed.date || this.entryInitialDate;
    this.entryModal = kind;
  }

  onEditRequested(entry: HistoryEntry): void {
    if (entry.id == null) {
      return;
    }
    this.editOriginalId = entry.id;
    this.editOriginalType = entry.type === 'INCOME' ? 'INCOME' : 'EXPENSE';
    this.entryMode = 'edit';
    if (entry.type === 'INCOME') {
      this.incomeApi.getIncome(entry.id).subscribe({
        next: (detail) => this.applyEditDetail(entry, {
          date: detail.date,
          amount: detail.amount,
          description: detail.description,
          is_recurring: detail.is_recurring,
          income_source_id: detail.income_source_id
        }, 'income')
      });
      return;
    }
    this.expenseApi.getExpense(entry.id).subscribe({
      next: (detail) => this.applyEditDetail(entry, {
        date: detail.date,
        amount: detail.amount,
        description: detail.description,
        is_recurring: detail.is_recurring,
        expense_category_id: detail.expense_category_id,
        payment_method_id: detail.payment_method_id
      }, 'expense')
    });
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
        this.dayDialog?.selected.clear();
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
      this.scopeMode = 'delete';
      this.scopeOpen = true;
      return;
    }
    this.submitDelete();
  }

  onScope(scope: string): void {
    this.scopeOpen = false;
    if (this.scopeMode === 'edit') {
      this.commitEdit(scope);
      return;
    }
    this.submitDelete(scope);
  }

  onScopeClose(): void {
    this.scopeOpen = false;
    if (this.scopeMode === 'delete') {
      this.pendingItems = [];
    }
  }

  closeList(): void {
    this.listOpen = false;
    this.pendingItems = [];
  }

  private commitEdit(scope?: string): void {
    const pending = this.pendingEdit;
    const id = this.editOriginalId;
    const original = this.editOriginalType;
    const kind = this.entryModal;
    if (!pending || id == null || !original || !kind) {
      return;
    }
    const payload = { ...pending.payload };
    if (scope) {
      payload['recurrence_scope'] = scope;
    }
    const sameType = (original === 'INCOME' && kind === 'income') || (original === 'EXPENSE' && kind === 'expense');
    const finish = () => {
      const date = pending.date;
      this.closeEntryModal();
      this.goToMonthOf(date);
      this.reload(true);
    };
    if (sameType) {
      const req$ = original === 'INCOME'
        ? this.incomeApi.updateIncome(id, payload)
        : this.expenseApi.updateExpense(id, payload);
      req$.subscribe({ next: finish, error: () => undefined });
      return;
    }
    const del$ = original === 'INCOME'
      ? this.incomeApi.deleteIncome(id, scope)
      : this.expenseApi.deleteExpense(id, scope);
    const create$ = kind === 'income'
      ? this.incomeApi.createIncome(payload)
      : this.expenseApi.createExpense(payload);
    del$.pipe(switchMap(() => create$)).subscribe({ next: finish, error: () => undefined });
  }

  private applyEditDetail(
    entry: HistoryEntry,
    detail: IncomeSeed & ExpenseSeed & { is_recurring?: boolean },
    kind: 'income' | 'expense'
  ): void {
    this.editIsRecurring = !!detail.is_recurring || !!entry.is_recurring || !!entry.is_recurring_projection;
    this.entrySeed = {
      date: detail.date,
      amount: detail.amount,
      description: detail.description || '',
      income_source_id: detail.income_source_id ?? null,
      expense_category_id: detail.expense_category_id ?? null,
      payment_method_id: detail.payment_method_id ?? null,
      repeat: !!detail.is_recurring
    };
    this.openEntry(kind, detail.date || entry.label);
  }

  private openEntry(kind: 'income' | 'expense', date: string): void {
    this.entryModal = kind;
    this.entryInitialDate = date;
  }

  private goToMonthOf(date: string): void {
    const parts = date.split('-').map(Number);
    const year = parts[0];
    const month = parts[1];
    if (!year || !month) {
      return;
    }
    this.month = new Date(year, month - 1, 1);
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
