package com.financia.report.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class ReportResponse {
    private final Period period;
    private final Totals totals;
    private final List<CategoryBreakdown> categoryBreakdown;
    private final Projections projections;

    @Getter
    @Builder
    public static class Period {
        private final LocalDate from;
        private final LocalDate to;
    }

    @Getter
    @Builder
    public static class Totals {
        private final BigDecimal income;
        private final BigDecimal expensesConfirmed;
        private final BigDecimal expensesCreditCard;
        private final BigDecimal savings;
    }

    @Getter
    @Builder
    public static class CategoryBreakdown {
        private final String category;
        private final BigDecimal amount;
        private final BigDecimal percentage;
    }

    @Getter
    @Builder
    public static class Point {
        private final LocalDate date;
        private final BigDecimal cumulative;
    }

    @Getter
    @Builder
    public static class Projections {
        private final List<Point> income;
        private final List<Point> expenses;
        private final List<Point> savings;
    }
}
