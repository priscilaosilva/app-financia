import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { environment } from '../../environments/environment';

@Injectable({ providedIn: 'root' })
export class ExpenseService {
  constructor(private http: HttpClient) {}

  createExpense(payload: unknown) {
    return this.http.post(`${environment.apiUrl}/expense`, payload);
  }

  updateExpense(id: number, payload: unknown) {
    return this.http.put(`${environment.apiUrl}/expense/${id}`, payload);
  }

  deleteExpense(id: number, recurrenceScope?: string) {
    return this.http.delete(`${environment.apiUrl}/expense/${id}`, { body: { recurrence_scope: recurrenceScope } });
  }

  getCategories() {
    return this.http.get<Array<{ id: number; name: string }>>(`${environment.apiUrl}/categories`);
  }

  createCategory(name: string) {
    return this.http.post(`${environment.apiUrl}/categories`, { name });
  }

  deleteCategory(id: number) {
    return this.http.delete(`${environment.apiUrl}/categories/${id}`);
  }

  getPaymentMethods() {
    return this.http.get<Array<{
      id: number;
      name: string;
      type: string;
      due_day?: number;
      billing_start_day?: number;
      billing_end_day?: number;
    }>>(`${environment.apiUrl}/payment-methods`);
  }

  createPaymentMethod(payload: unknown) {
    return this.http.post(`${environment.apiUrl}/payment-methods`, payload);
  }

  deletePaymentMethod(id: number) {
    return this.http.delete(`${environment.apiUrl}/payment-methods/${id}`);
  }
}
