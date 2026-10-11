package com.financia.history.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    @JsonProperty("has_movement")
    private final boolean hasMovement;
}
