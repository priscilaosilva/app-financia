import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ExpenseService } from '../expense.service';

export interface ExpenseSeed {
  date?: string;
  amount?: number | null;
  description?: string;
  expense_category_id?: number | null;
  payment_method_id?: number | null;
  repeat?: boolean;
  frequency?: string;
}

@Component({
  selector: 'app-expense-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './expense-form.component.html'
})
export class ExpenseFormComponent implements OnInit, OnChanges {
  @Input() initialDate = '';
  @Input() mode: 'create' | 'edit' = 'create';
  @Input() seed: ExpenseSeed = {};
  @Output() saved = new EventEmitter<string>();
  @Output() submitted = new EventEmitter<{ date: string; payload: Record<string, unknown> }>();
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
    this.applySeed();
    this.expense.getCategories().subscribe((items) => this.categories = items);
    this.expense.getPaymentMethods().subscribe((items) => this.methods = items);
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes['seed'] && !changes['seed'].firstChange) {
      this.applySeed();
    }
  }

  private applySeed(): void {
    this.form.patchValue({
      date: this.seed.date || this.initialDate || '',
      amount: this.seed.amount ?? null,
      description: this.seed.description || '',
      expense_category_id: this.seed.expense_category_id ?? null,
      payment_method_id: this.seed.payment_method_id ?? null,
      repeat: this.seed.repeat ?? false,
      frequency: this.seed.frequency || 'MONTHLY'
    });
  }

  snapshot(): ExpenseSeed {
    const value = this.form.getRawValue();
    return {
      date: value.date || '',
      amount: value.amount,
      description: value.description || '',
      repeat: !!value.repeat,
      frequency: value.frequency || 'MONTHLY'
    };
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
    if (this.mode === 'edit') {
      this.submitted.emit({ date: String(value.date ?? ''), payload });
      return;
    }
    this.expense.createExpense(payload).subscribe({
      next: () => this.saved.emit(String(value.date ?? '')),
      error: () => undefined
    });
  }
}
