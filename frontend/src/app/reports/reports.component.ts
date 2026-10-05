import { CommonModule } from '@angular/common';
import { AfterViewInit, Component, ElementRef, ViewChild } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Chart } from 'chart.js/auto';
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

  constructor(private reports: ReportsService) {}

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
    this.reports.getReport(this.params()).subscribe((report) => {
      this.report = report;
      this.renderChart();
    });
  }

  exportCsv(): void {
    this.reports.exportCsv(this.params());
  }

  private renderChart(): void {
    if (!this.chartRef || !this.report) {
      return;
    }
    this.chart?.destroy();
    this.chart = new Chart(this.chartRef.nativeElement, {
      type: 'line',
      data: {
        labels: this.report.projections.savings.map((p) => p.date),
        datasets: [
          { label: 'Entradas', data: this.report.projections.income.map((p) => p.cumulative), borderColor: '#1f6b4a' },
          { label: 'Saídas', data: this.report.projections.expenses.map((p) => p.cumulative), borderColor: '#9b3d3d' },
          { label: 'Economia', data: this.report.projections.savings.map((p) => p.cumulative), borderColor: '#c9842a' }
        ]
      }
    });
  }
}
