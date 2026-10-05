import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class IncomeService {
  constructor(private http: HttpClient) {}

  createIncome(payload: unknown) {
    return this.http.post(`${environment.apiUrl}/income`, payload);
  }

  updateIncome(id: number, payload: unknown) {
    return this.http.put(`${environment.apiUrl}/income/${id}`, payload);
  }

  deleteIncome(id: number, recurrenceScope?: string) {
    return this.http.delete(`${environment.apiUrl}/income/${id}`, { body: { recurrence_scope: recurrenceScope } });
  }

  getIncomeSources() {
    return this.http.get<Array<{ id: number; name: string }>>(`${environment.apiUrl}/income-sources`);
  }

  createIncomeSource(name: string) {
    return this.http.post(`${environment.apiUrl}/income-sources`, { name });
  }

  deleteIncomeSource(id: number) {
    return this.http.delete(`${environment.apiUrl}/income-sources/${id}`);
  }
}
