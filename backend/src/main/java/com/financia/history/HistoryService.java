package com.financia.history;

import com.financia.common.BusinessException;
import com.financia.expense.ExpenseEntry;
import com.financia.expense.ExpenseEntryRepository;
import com.financia.expense.PaymentStatus;
import com.financia.history.dto.BalanceResponse;
import com.financia.history.dto.HistoryEntryDto;
import com.financia.history.dto.HistoryResponse;
import com.financia.history.dto.HistoryRowDto;
import com.financia.income.IncomeEntry;
import com.financia.income.IncomeEntryRepository;
import com.financia.paymentmethod.PaymentMethod;
import com.financia.paymentmethod.PaymentMethodRepository;
import com.financia.paymentmethod.PaymentMethodType;
import com.financia.recurrence.ProjectedOccurrence;
import com.financia.recurrence.RecurrenceProjectionService;
import com.financia.recurrence.RecurrenceRule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@Service
@RequiredArgsConstructor
public class HistoryService {

    private final IncomeEntryRepository incomeEntryRepository;
    private final ExpenseEntryRepository expenseEntryRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final RecurrenceProjectionService recurrenceProjectionService;

    @Transactional(readOnly = true)
    public BalanceResponse balance(Long userId) {
        LocalDate today = LocalDate.now();
        BigDecimal income = incomeEntryRepository.sumAmountByUserIdAndDateLessThanEqual(userId, today);
        BigDecimal confirmed = expenseEntryRepository.sumAmountByUserIdAndStatusAndDateLessThanEqual(
                userId, PaymentStatus.CONFIRMED, today);
        BigDecimal bills = creditCardBillsUntil(userId, today);
        return new BalanceResponse(income.subtract(confirmed).subtract(bills), today);
    }

    @Transactional(readOnly = true)
    public HistoryResponse history(Long userId, LocalDate from, LocalDate to) {
        if (from == null || to == null) {
            throw new BusinessException("Período from/to é obrigatório", "from");
        }
        if (to.isBefore(from)) {
            throw new BusinessException("Data final deve ser posterior à inicial", "to");
        }

        Map<LocalDate, List<HistoryEntryDto>> byDate = new TreeMap<>();
        addPersistedIncomes(userId, from, to, byDate);
        addPersistedExpenses(userId, from, to, byDate);
        addProjectedIncomes(userId, from, to, byDate);
        addProjectedExpenses(userId, from, to, byDate);
        addCreditCardBills(userId, from, to, byDate);

        BigDecimal running = openingBalance(userId, from.minusDays(1));
        List<HistoryRowDto> rows = new ArrayList<>();
        for (Map.Entry<LocalDate, List<HistoryEntryDto>> entry : byDate.entrySet()) {
            BigDecimal dailyIncome = BigDecimal.ZERO;
            BigDecimal dailyConfirmed = BigDecimal.ZERO;
            BigDecimal dailyPending = BigDecimal.ZERO;
            for (HistoryEntryDto item : entry.getValue()) {
                if ("INCOME".equals(item.getType())) {
                    dailyIncome = dailyIncome.add(item.getAmount());
                    running = running.add(item.getAmount());
                } else if ("EXPENSE".equals(item.getType()) && "PENDING_CREDIT".equals(item.getStatus())) {
                    dailyPending = dailyPending.add(item.getAmount());
                } else if ("EXPENSE".equals(item.getType()) || "CREDIT_CARD_BILL".equals(item.getType())) {
                    dailyConfirmed = dailyConfirmed.add(item.getAmount());
                    running = running.subtract(item.getAmount());
                }
            }
            rows.add(HistoryRowDto.builder()
                    .date(entry.getKey())
                    .entries(entry.getValue())
                    .dailyIncome(dailyIncome)
                    .dailyExpenseConfirmed(dailyConfirmed)
                    .dailyExpensePending(dailyPending)
                    .runningBalance(running)
                    .build());
        }
        return new HistoryResponse(from, to, rows);
    }

    private void addPersistedIncomes(Long userId, LocalDate from, LocalDate to, Map<LocalDate, List<HistoryEntryDto>> byDate) {
        for (IncomeEntry entry : incomeEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to)) {
            add(byDate, entry.getDate(), HistoryEntryDto.builder()
                    .id(entry.getId())
                    .type("INCOME")
                    .amount(entry.getAmount())
                    .label(label(entry.getDescription(), entry.getSourceNameSnapshot()))
                    .status("CONFIRMED")
                    .isRecurringProjection(false)
                    .date(entry.getDate())
                    .build());
        }
    }

    private void addPersistedExpenses(Long userId, LocalDate from, LocalDate to, Map<LocalDate, List<HistoryEntryDto>> byDate) {
        for (ExpenseEntry entry : expenseEntryRepository.findByUserIdAndDateBetweenOrderByDateAsc(userId, from, to)) {
            add(byDate, entry.getDate(), HistoryEntryDto.builder()
                    .id(entry.getId())
                    .type("EXPENSE")
                    .amount(entry.getAmount())
                    .label(label(entry.getDescription(), entry.getCategoryNameSnapshot()))
                    .status(entry.getPaymentStatus().name())
                    .paymentMethod(entry.getPaymentMethodNameSnapshot())
                    .categoryNameSnapshot(entry.getCategoryNameSnapshot())
                    .isRecurringProjection(false)
                    .date(entry.getDate())
                    .build());
        }
    }

    private void addProjectedIncomes(Long userId, LocalDate from, LocalDate to, Map<LocalDate, List<HistoryEntryDto>> byDate) {
        for (IncomeEntry base : incomeEntryRepository.findByUserIdAndRecurrenceRuleIsNotNull(userId)) {
            RecurrenceRule rule = base.getRecurrenceRule();
            if (rule == null) {
                continue;
            }
            for (ProjectedOccurrence occurrence : recurrenceProjectionService.generateOccurrences(
                    rule, base.getAmount(), base.getDescription(), from, to)) {
                if (occurrence.isCancelled() || occurrence.getDate().equals(base.getDate())) {
                    continue;
                }
                add(byDate, occurrence.getDate(), HistoryEntryDto.builder()
                        .id(base.getId())
                        .type("INCOME")
                        .amount(occurrence.getAmount())
                        .label(label(occurrence.getDescription(), base.getSourceNameSnapshot()))
                        .status("CONFIRMED")
                        .isRecurringProjection(true)
                        .date(occurrence.getDate())
                        .build());
            }
        }
    }

    private void addProjectedExpenses(Long userId, LocalDate from, LocalDate to, Map<LocalDate, List<HistoryEntryDto>> byDate) {
        for (ExpenseEntry base : expenseEntryRepository.findByUserIdAndRecurrenceRuleIsNotNull(userId)) {
            RecurrenceRule rule = base.getRecurrenceRule();
            if (rule == null) {
                continue;
            }
            for (ProjectedOccurrence occurrence : recurrenceProjectionService.generateOccurrences(
                    rule, base.getAmount(), base.getDescription(), from, to)) {
                if (occurrence.isCancelled() || occurrence.getDate().equals(base.getDate())) {
                    continue;
                }
                add(byDate, occurrence.getDate(), HistoryEntryDto.builder()
                        .id(base.getId())
                        .type("EXPENSE")
                        .amount(occurrence.getAmount())
                        .label(label(occurrence.getDescription(), base.getCategoryNameSnapshot()))
                        .status(base.getPaymentStatus().name())
                        .paymentMethod(base.getPaymentMethodNameSnapshot())
                        .categoryNameSnapshot(base.getCategoryNameSnapshot())
                        .isRecurringProjection(true)
                        .date(occurrence.getDate())
                        .build());
            }
        }
    }

    private void addCreditCardBills(Long userId, LocalDate from, LocalDate to, Map<LocalDate, List<HistoryEntryDto>> byDate) {
        for (PaymentMethod card : paymentMethodRepository.findByUserIdAndTypeAndActiveTrue(userId, PaymentMethodType.CREDIT_CARD)) {
            for (LocalDate due : dueDatesBetween(card, from, to)) {
                LocalDate[] period = billingPeriod(card, due);
                BigDecimal total = pendingTotal(userId, card.getId(), period[0], period[1]);
                if (total.compareTo(BigDecimal.ZERO) <= 0) {
                    continue;
                }
                add(byDate, due, HistoryEntryDto.builder()
                        .id(null)
                        .type("CREDIT_CARD_BILL")
                        .amount(total)
                        .label("Fatura " + card.getName())
                        .status("CONFIRMED")
                        .paymentMethod(card.getName())
                        .isRecurringProjection(false)
                        .date(due)
                        .build());
            }
        }
    }

    private BigDecimal openingBalance(Long userId, LocalDate until) {
        BigDecimal income = incomeEntryRepository.sumAmountByUserIdAndDateLessThanEqual(userId, until);
        BigDecimal confirmed = expenseEntryRepository.sumAmountByUserIdAndStatusAndDateLessThanEqual(
                userId, PaymentStatus.CONFIRMED, until);
        BigDecimal bills = creditCardBillsUntil(userId, until);
        return income.subtract(confirmed).subtract(bills);
    }

    private BigDecimal creditCardBillsUntil(Long userId, LocalDate until) {
        BigDecimal total = BigDecimal.ZERO;
        for (PaymentMethod card : paymentMethodRepository.findByUserIdAndTypeAndActiveTrue(userId, PaymentMethodType.CREDIT_CARD)) {
            LocalDate start = until.minusYears(5);
            for (LocalDate due : dueDatesBetween(card, start, until)) {
                LocalDate[] period = billingPeriod(card, due);
                total = total.add(pendingTotal(userId, card.getId(), period[0], period[1]));
            }
        }
        return total;
    }

    private BigDecimal pendingTotal(Long userId, Long cardId, LocalDate from, LocalDate to) {
        return expenseEntryRepository
                .findByUserIdAndPaymentMethodIdAndPaymentStatusAndDateBetween(
                        userId, cardId, PaymentStatus.PENDING_CREDIT, from, to)
                .stream()
                .map(ExpenseEntry::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private List<LocalDate> dueDatesBetween(PaymentMethod card, LocalDate from, LocalDate to) {
        List<LocalDate> dates = new ArrayList<>();
        YearMonth cursor = YearMonth.from(from).minusMonths(1);
        YearMonth last = YearMonth.from(to).plusMonths(1);
        while (!cursor.isAfter(last)) {
            LocalDate due = safeDay(cursor, card.getDueDay());
            if (!due.isBefore(from) && !due.isAfter(to)) {
                dates.add(due);
            }
            cursor = cursor.plusMonths(1);
        }
        return dates;
    }

    private LocalDate[] billingPeriod(PaymentMethod card, LocalDate dueDate) {
        int startDay = card.getBillingStartDay() != null ? card.getBillingStartDay() : card.getDueDay();
        int endDay = card.getBillingEndDay() != null ? card.getBillingEndDay() : card.getDueDay();
        YearMonth dueMonth = YearMonth.from(dueDate);
        LocalDate end = safeDay(dueMonth, endDay);
        LocalDate start = startDay > endDay
                ? safeDay(dueMonth.minusMonths(1), startDay)
                : safeDay(dueMonth, startDay);
        return new LocalDate[]{start, end};
    }

    private LocalDate safeDay(YearMonth month, Integer day) {
        int safe = Math.min(day != null ? day : 1, month.lengthOfMonth());
        return month.atDay(safe);
    }

    private void add(Map<LocalDate, List<HistoryEntryDto>> byDate, LocalDate date, HistoryEntryDto dto) {
        byDate.computeIfAbsent(date, key -> new ArrayList<>()).add(dto);
        byDate.get(date).sort(Comparator.comparing(HistoryEntryDto::getType));
    }

    private String label(String description, String fallback) {
        if (description != null && !description.isBlank()) {
            return fallback != null ? description + " — " + fallback : description;
        }
        return fallback != null ? fallback : "Lançamento";
    }
}
