import { CommonModule } from '@angular/common';
import { Component, EventEmitter, OnInit, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ExpenseService } from '../expense.service';

@Component({
  selector: 'app-expense-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './expense-form.component.html'
})
export class ExpenseFormComponent implements OnInit {
  @Output() saved = new EventEmitter<void>();
  categories: Array<{ id: number; name: string }> = [];
  methods: Array<{ id: number; name: string; type: string }> = [];
  form = this.fb.group({
    date: ['', Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    expense_category_id: [null as number | null],
    payment_method_id: [null as number | null],
    description: [''],
    repeat: [false],
    frequency: ['MONTHLY']
  });

  constructor(private fb: FormBuilder, private expense: ExpenseService) {}

  ngOnInit(): void {
    this.expense.getCategories().subscribe((items) => this.categories = items);
    this.expense.getPaymentMethods().subscribe((items) => this.methods = items);
  }

  selectedIsCard(): boolean {
    const id = this.form.value.payment_method_id;
    return this.methods.some((method) => method.id === id && method.type === 'CREDIT_CARD');
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const payload: Record<string, unknown> = {
      date: value.date,
      amount: value.amount,
      expense_category_id: value.expense_category_id || null,
      payment_method_id: value.payment_method_id || null,
      description: value.description
    };
    if (value.repeat) {
      payload['recurrence'] = { frequency: value.frequency, start_date: value.date };
    }
    this.expense.createExpense(payload).subscribe(() => {
      this.form.reset({ date: value.date, repeat: false, frequency: 'MONTHLY' });
      this.saved.emit();
    });
  }
}
