package com.financia.history.dto;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Getter
@Builder
public class HistoryRowDto {
    private final LocalDate date;
    private final List<HistoryEntryDto> entries;
    private final BigDecimal dailyIncome;
    private final BigDecimal dailyExpenseConfirmed;
    private final BigDecimal dailyExpensePending;
    private final BigDecimal runningBalance;
}
