import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Chart } from 'chart.js/auto';
import { switchMap } from 'rxjs/operators';
import { HistoryResponse, HistoryService } from '../history/history.service';
import { ReportResponse, ReportsService } from './reports.service';

@Component({
  selector: 'app-reports',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './reports.component.html'
})
export class ReportsComponent implements AfterViewInit {
  @ViewChild('chart') chartRef?: ElementRef<HTMLCanvasElement>;
  filter = 'MONTH';
  year = new Date().getFullYear();
  month = new Date().getMonth() + 1;
  from = '';
  to = '';
  report?: ReportResponse;
  private chart?: Chart;

  constructor(private reports: ReportsService, private historyApi: HistoryService) {}

  ngAfterViewInit(): void {
    this.generate();
  }

  params(): Record<string, string | number> {
    if (this.filter === 'RANGE') {
      return { from: this.from, to: this.to };
    }
    if (this.filter === 'YEAR') {
      return { filter: 'YEAR', year: this.year };
    }
    return { filter: 'MONTH', year: this.year, month: this.month };
  }

  generate(): void {
    this.reports.getReport(this.params()).pipe(
      switchMap((report) => {
        this.report = report;
        return this.historyApi.getHistory(report.period.from, report.period.to);
      })
    ).subscribe({
      next: (history) => setTimeout(() => this.renderChart(history)),
      error: () => setTimeout(() => this.renderChart())
    });
  }

  exportCsv(): void {
    this.reports.exportCsv(this.params());
  }

  private renderChart(history?: HistoryResponse): void {
    if (!this.chartRef) {
      return;
    }
    const rows = history?.rows ?? [];
    this.chart?.destroy();
    this.chart = new Chart(this.chartRef.nativeElement, {
      type: 'line',
      data: {
        labels: rows.map((row) => row.date),
        datasets: [
          {
            label: 'Saldo',
            data: rows.map((row) => row.running_balance),
            borderColor: '#c9842a'
          },
          {
            label: 'Entradas do dia',
            data: rows.map((row) => row.daily_income),
            borderColor: '#1f6b4a'
          },
          {
            label: 'Saídas do dia',
            data: rows.map((row) => row.daily_expense_confirmed + row.daily_expense_pending),
            borderColor: '#9b3d3d'
          }
        ]
      }
    });
  }
}
