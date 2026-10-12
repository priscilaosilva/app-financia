import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface HistoryEntry {
  id: number | null;
  type: string;
  amount: number;
  label: string;
  status: string;
  payment_method?: string;
  is_recurring_projection: boolean;
  is_recurring?: boolean;
}

export interface HistoryRow {
  date: string;
  entries: HistoryEntry[];
  daily_income: number;
  daily_expense_confirmed: number;
  daily_expense_pending: number;
  running_balance: number;
  has_movement?: boolean;
}

export interface HistoryResponse {
  from: string;
  to: string;
  rows: HistoryRow[];
}

@Injectable({ providedIn: 'root' })
export class HistoryService {
  constructor(private http: HttpClient) {}

  getHistory(from: string, to: string): Observable<HistoryResponse> {
    return this.http.get<HistoryResponse>(`${environment.apiUrl}/history`, { params: { from, to } });
  }

  getBalance() {
    return this.http.get<{ balance: number; calculated_at: string }>(`${environment.apiUrl}/balance`);
  }

  deleteDayEntries(date: string, items: Array<{ id: number; type: string }>, recurrenceScope?: string) {
    return this.http.post(`${environment.apiUrl}/history/day-deletions`, {
      date,
      items,
      recurrence_scope: recurrenceScope
    });
  }
}
