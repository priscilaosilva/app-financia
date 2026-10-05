package com.financia.report;

import com.financia.common.BusinessException;
import com.financia.expense.ExpenseEntry;
import com.financia.expense.ExpenseEntryRepository;
import com.financia.expense.PaymentStatus;
import com.financia.income.IncomeEntry;
import com.financia.income.IncomeEntryRepository;
import com.financia.report.dto.ReportResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class ReportService {

    private final IncomeEntryRepository incomeEntryRepository;
    private final ExpenseEntryRepository expenseEntryRepository;

    @Transactional(readOnly = true)
    public ReportResponse generate(Long userId, LocalDate from, LocalDate to, String filter, Integer year, Integer month) {
        LocalDate[] period = resolvePeriod(from, to, filter, year, month);
        LocalDate start = period[0];
        LocalDate end = period[1];

        List<IncomeEntry> incomes = incomeEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end);
        List<ExpenseEntry> expenses = expenseEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, start, end);

        BigDecimal totalIncome = incomes.stream().map(IncomeEntry::getAmount).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalConfirmed = expenses.stream()
                .filter(e -> e.getPaymentStatus() == PaymentStatus.CONFIRMED)
                .map(ExpenseEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalCredit = expenses.stream()
                .filter(e -> e.getPaymentStatus() == PaymentStatus.PENDING_CREDIT)
                .map(ExpenseEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalExpenses = totalConfirmed.add(totalCredit);
        BigDecimal savings = totalIncome.subtract(totalExpenses);

        Map<String, BigDecimal> byCategory = new LinkedHashMap<>();
        for (ExpenseEntry expense : expenses) {
            String name = expense.getCategoryNameSnapshot() != null ? expense.getCategoryNameSnapshot() : "Sem categoria";
            byCategory.merge(name, expense.getAmount(), BigDecimal::add);
        }
        List<ReportResponse.CategoryBreakdown> breakdown = byCategory.entrySet().stream()
                .sorted(Map.Entry.<String, BigDecimal>comparingByValue().reversed())
                .map(entry -> ReportResponse.CategoryBreakdown.builder()
                        .category(entry.getKey())
                        .amount(entry.getValue())
                        .percentage(totalExpenses.compareTo(BigDecimal.ZERO) == 0
                                ? BigDecimal.ZERO
                                : entry.getValue().multiply(BigDecimal.valueOf(100)).divide(totalExpenses, 2, RoundingMode.HALF_UP))
                        .build())
                .toList();

        Map<LocalDate, BigDecimal> incomeByDate = new TreeMap<>();
        Map<LocalDate, BigDecimal> expenseByDate = new TreeMap<>();
        incomes.forEach(i -> incomeByDate.merge(i.getDate(), i.getAmount(), BigDecimal::add));
        expenses.forEach(e -> expenseByDate.merge(e.getDate(), e.getAmount(), BigDecimal::add));

        List<ReportResponse.Point> incomePoints = cumulative(incomeByDate);
        List<ReportResponse.Point> expensePoints = cumulative(expenseByDate);
        List<ReportResponse.Point> savingsPoints = savingsPoints(incomeByDate, expenseByDate);

        return ReportResponse.builder()
                .period(ReportResponse.Period.builder().from(start).to(end).build())
                .totals(ReportResponse.Totals.builder()
                        .income(totalIncome)
                        .expensesConfirmed(totalConfirmed)
                        .expensesCreditCard(totalCredit)
                        .savings(savings)
                        .build())
                .categoryBreakdown(breakdown)
                .projections(ReportResponse.Projections.builder()
                        .income(incomePoints)
                        .expenses(expensePoints)
                        .savings(savingsPoints)
                        .build())
                .build();
    }

    public String toCsv(Long userId, LocalDate from, LocalDate to, String filter, Integer year, Integer month) {
        LocalDate[] period = resolvePeriod(from, to, filter, year, month);
        StringBuilder csv = new StringBuilder("Data,Tipo,Descricao,Categoria,Fonte,Valor\n");
        incomeEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, period[0], period[1])
                .forEach(entry -> csv.append(String.format("%s,Entrada,%s,—,%s,%s%n",
                        entry.getDate(),
                        safe(entry.getDescription()),
                        safe(entry.getSourceNameSnapshot()),
                        entry.getAmount())));
        expenseEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, period[0], period[1])
                .forEach(entry -> csv.append(String.format("%s,Saída,%s,%s,%s,-%s%n",
                        entry.getDate(),
                        safe(entry.getDescription()),
                        safe(entry.getCategoryNameSnapshot()),
                        safe(entry.getPaymentMethodNameSnapshot()),
                        entry.getAmount())));
        return csv.toString();
    }

    public String filename(LocalDate from, LocalDate to, String filter, Integer year, Integer month) {
        LocalDate[] period = resolvePeriod(from, to, filter, year, month);
        return "relatorio-" + period[0] + "-a-" + period[1] + ".csv";
    }

    private LocalDate[] resolvePeriod(LocalDate from, LocalDate to, String filter, Integer year, Integer month) {
        if (from != null && to != null) {
            return new LocalDate[]{from, to};
        }
        if ("MONTH".equalsIgnoreCase(filter) && year != null && month != null) {
            YearMonth ym = YearMonth.of(year, month);
            return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
        }
        if ("YEAR".equalsIgnoreCase(filter) && year != null) {
            return new LocalDate[]{LocalDate.of(year, 1, 1), LocalDate.of(year, 12, 31)};
        }
        throw new BusinessException("Informe um período: from/to, MONTH ou YEAR", "filter");
    }

    private List<ReportResponse.Point> cumulative(Map<LocalDate, BigDecimal> byDate) {
        List<ReportResponse.Point> points = new ArrayList<>();
        BigDecimal running = BigDecimal.ZERO;
        for (Map.Entry<LocalDate, BigDecimal> entry : byDate.entrySet()) {
            running = running.add(entry.getValue());
            points.add(ReportResponse.Point.builder().date(entry.getKey()).cumulative(running).build());
        }
        return points;
    }

    private List<ReportResponse.Point> savingsPoints(Map<LocalDate, BigDecimal> incomes, Map<LocalDate, BigDecimal> expenses) {
        TreeMap<LocalDate, Boolean> dates = new TreeMap<>();
        incomes.keySet().forEach(d -> dates.put(d, true));
        expenses.keySet().forEach(d -> dates.put(d, true));
        List<ReportResponse.Point> points = new ArrayList<>();
        BigDecimal incomeRun = BigDecimal.ZERO;
        BigDecimal expenseRun = BigDecimal.ZERO;
        for (LocalDate date : dates.keySet()) {
            incomeRun = incomeRun.add(incomes.getOrDefault(date, BigDecimal.ZERO));
            expenseRun = expenseRun.add(expenses.getOrDefault(date, BigDecimal.ZERO));
            points.add(ReportResponse.Point.builder().date(date).cumulative(incomeRun.subtract(expenseRun)).build());
        }
        return points;
    }

    private String safe(String value) {
        if (value == null || value.isBlank()) {
            return "—";
        }
        return value.replace(",", " ");
    }
}
