import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { environment } from '../../environments/environment';

export interface ReportResponse {
  period: { from: string; to: string };
  totals: {
    income: number;
    expenses_confirmed: number;
    expenses_credit_card: number;
    savings: number;
  };
  category_breakdown: Array<{ category: string; amount: number; percentage: number }>;
  projections: {
    income: Array<{ date: string; cumulative: number }>;
    expenses: Array<{ date: string; cumulative: number }>;
    savings: Array<{ date: string; cumulative: number }>;
  };
}

@Injectable({ providedIn: 'root' })
export class ReportsService {
  constructor(private http: HttpClient) {}

  getReport(params: Record<string, string | number>): Observable<ReportResponse> {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => httpParams = httpParams.set(key, String(value)));
    return this.http.get<ReportResponse>(`${environment.apiUrl}/reports`, { params: httpParams });
  }

  exportCsv(params: Record<string, string | number>): void {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => httpParams = httpParams.set(key, String(value)));
    this.http.get(`${environment.apiUrl}/reports/export`, { params: httpParams, responseType: 'blob' }).subscribe((blob) => {
      const url = URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.download = 'relatorio.csv';
      link.click();
      URL.revokeObjectURL(url);
    });
  }
}
