import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnChanges, OnInit, Output, SimpleChanges } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { IncomeService } from '../income.service';

export interface IncomeSeed {
  date?: string;
  amount?: number | null;
  description?: string;
  income_source_id?: number | null;
  repeat?: boolean;
  frequency?: string;
}

@Component({
  selector: 'app-income-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './income-form.component.html'
})
export class IncomeFormComponent implements OnInit, OnChanges {
  @Input() initialDate = '';
  @Input() mode: 'create' | 'edit' = 'create';
  @Input() seed: IncomeSeed = {};
  @Output() saved = new EventEmitter<string>();
  @Output() submitted = new EventEmitter<{ date: string; payload: Record<string, unknown> }>();
  sources: Array<{ id: number; name: string }> = [];
  form = this.fb.group({
    date: ['', Validators.required],
    amount: [null as number | null, [Validators.required, Validators.min(0.01)]],
    income_source_id: [null as number | null],
    description: [''],
    repeat: [false],
    frequency: ['MONTHLY']
  });

  constructor(private fb: FormBuilder, private income: IncomeService) {}

  ngOnInit(): void {
    this.applySeed();
    this.income.getIncomeSources().subscribe((sources) => this.sources = sources);
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
      income_source_id: this.seed.income_source_id ?? null,
      repeat: this.seed.repeat ?? false,
      frequency: this.seed.frequency || 'MONTHLY'
    });
  }

  snapshot(): IncomeSeed {
    const value = this.form.getRawValue();
    return {
      date: value.date || '',
      amount: value.amount,
      description: value.description || '',
      repeat: !!value.repeat,
      frequency: value.frequency || 'MONTHLY'
    };
  }

  submit(): void {
    if (this.form.invalid) {
      return;
    }
    const value = this.form.getRawValue();
    const payload: Record<string, unknown> = {
      date: value.date,
      amount: value.amount,
      income_source_id: value.income_source_id || null,
      description: value.description
    };
    if (value.repeat) {
      payload['recurrence'] = { frequency: value.frequency, start_date: value.date };
    }
    if (this.mode === 'edit') {
      this.submitted.emit({ date: String(value.date ?? ''), payload });
      return;
    }
    this.income.createIncome(payload).subscribe({
      next: () => this.saved.emit(String(value.date ?? '')),
      error: () => undefined
    });
  }
}
