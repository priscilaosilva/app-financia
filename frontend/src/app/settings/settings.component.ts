import { CommonModule } from '@angular/common';
import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { IncomeService } from '../income/income.service';
import { ExpenseService } from '../expense/expense.service';
import { ThemeService } from '../shared/services/theme.service';

@Component({
  selector: 'app-settings',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './settings.component.html'
})
export class SettingsComponent implements OnInit {
  sources: Array<{ id: number; name: string }> = [];
  categories: Array<{ id: number; name: string }> = [];
  methods: Array<{ id: number; name: string; type: string }> = [];
  sourceName = '';
  categoryName = '';
  methodName = '';
  methodType = 'CHECKING';
  dueDay?: number;
  billingStart?: number;
  billingEnd?: number;

  constructor(
    public income: IncomeService,
    public expense: ExpenseService,
    public theme: ThemeService
  ) {}

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.income.getIncomeSources().subscribe((items) => this.sources = items);
    this.expense.getCategories().subscribe((items) => this.categories = items);
    this.expense.getPaymentMethods().subscribe((items) => this.methods = items);
  }

  addSource(): void {
    this.income.createIncomeSource(this.sourceName).subscribe(() => {
      this.sourceName = '';
      this.reload();
    });
  }

  addCategory(): void {
    this.expense.createCategory(this.categoryName).subscribe(() => {
      this.categoryName = '';
      this.reload();
    });
  }

  addMethod(): void {
    const payload: Record<string, unknown> = { name: this.methodName, type: this.methodType };
    if (this.methodType === 'CREDIT_CARD') {
      payload['due_day'] = this.dueDay;
      payload['billing_start_day'] = this.billingStart;
      payload['billing_end_day'] = this.billingEnd;
    }
    this.expense.createPaymentMethod(payload).subscribe(() => {
      this.methodName = '';
      this.reload();
    });
  }

  removeSource(id: number): void {
    this.income.deleteIncomeSource(id).subscribe(() => this.reload());
  }

  removeCategory(id: number): void {
    this.expense.deleteCategory(id).subscribe(() => this.reload());
  }

  removeMethod(id: number): void {
    this.expense.deletePaymentMethod(id).subscribe(() => this.reload());
  }
}
