import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { HistoryResponse, HistoryService } from './history.service';
import { IncomeFormComponent } from '../income/income-form/income-form.component';
import { ExpenseFormComponent } from '../expense/expense-form/expense-form.component';
import { RecurrenceScopeDialogComponent } from '../shared/components/recurrence-scope-dialog/recurrence-scope-dialog.component';
import { IncomeService } from '../income/income.service';
import { ExpenseService } from '../expense/expense.service';

@Component({
  selector: 'app-history',
  standalone: true,
  imports: [CommonModule, IncomeFormComponent, ExpenseFormComponent, RecurrenceScopeDialogComponent],
  templateUrl: './history.component.html'
})
export class HistoryComponent implements OnInit {
  month = new Date();
  history?: HistoryResponse;
  balance = 0;
  showIncome = false;
  showExpense = false;
  dialogOpen = false;
  pendingDelete?: { id: number; type: 'INCOME' | 'EXPENSE' };

  constructor(
    private historyApi: HistoryService,
    private income: IncomeService,
    private expense: ExpenseService
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    const from = this.firstDay();
    const to = this.lastDay();
    this.historyApi.getHistory(from, to).subscribe((data) => this.history = data);
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

  askDelete(id: number | null, type: string, projected: boolean): void {
    if (!id) {
      return;
    }
    this.pendingDelete = { id, type: type === 'INCOME' ? 'INCOME' : 'EXPENSE' };
    if (projected) {
      this.dialogOpen = true;
      return;
    }
    this.confirmDelete();
  }

  onScope(scope: string): void {
    this.dialogOpen = false;
    this.confirmDelete(scope);
  }

  private confirmDelete(scope?: string): void {
    if (!this.pendingDelete) {
      return;
    }
    const action = this.pendingDelete.type === 'INCOME'
      ? this.income.deleteIncome(this.pendingDelete.id, scope)
      : this.expense.deleteExpense(this.pendingDelete.id, scope);
    action.subscribe(() => this.reload());
    this.pendingDelete = undefined;
  }

  private firstDay(): string {
    return new Date(this.month.getFullYear(), this.month.getMonth(), 1).toISOString().slice(0, 10);
  }

  private lastDay(): string {
    return new Date(this.month.getFullYear(), this.month.getMonth() + 1, 0).toISOString().slice(0, 10);
  }

  monthLabel(): string {
    return this.month.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  }
}
