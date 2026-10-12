package com.financia.income.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.financia.income.IncomeEntry;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class IncomeResponse {
    private final Long id;
    private final LocalDate date;
    private final BigDecimal amount;
    private final String source;
    private final Long incomeSourceId;
    private final String description;
    @JsonProperty("is_recurring")
    private final boolean isRecurring;

    public static IncomeResponse from(IncomeEntry entry) {
        return new IncomeResponse(
                entry.getId(),
                entry.getDate(),
                entry.getAmount(),
                entry.getSourceNameSnapshot(),
                entry.getIncomeSource() != null ? entry.getIncomeSource().getId() : null,
                entry.getDescription(),
                entry.getRecurrenceRule() != null
        );
    }
}
