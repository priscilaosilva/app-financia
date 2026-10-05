package com.financia.income.dto;

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
    private final String description;

    public static IncomeResponse from(IncomeEntry entry) {
        return new IncomeResponse(
                entry.getId(),
                entry.getDate(),
                entry.getAmount(),
                entry.getSourceNameSnapshot(),
                entry.getDescription()
        );
    }
}
