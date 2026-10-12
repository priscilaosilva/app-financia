import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, OnInit, Output } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { IncomeService } from '../income.service';

@Component({
  selector: 'app-income-form',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './income-form.component.html'
})
export class IncomeFormComponent implements OnInit {
  @Input() initialDate = '';
  @Output() saved = new EventEmitter<string>();
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
    this.form.patchValue({ date: this.initialDate || '' });
    this.income.getIncomeSources().subscribe((sources) => this.sources = sources);
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
    this.income.createIncome(payload).subscribe({
      next: () => this.saved.emit(String(value.date ?? '')),
      error: () => undefined
    });
  }
}
