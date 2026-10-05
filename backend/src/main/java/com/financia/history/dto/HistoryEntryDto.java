package com.financia.history.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class HistoryEntryDto {
    private final Long id;
    private final String type;
    private final BigDecimal amount;
    private final String label;
    private final String status;
    private final String paymentMethod;
    private final String categoryNameSnapshot;
    @JsonProperty("is_recurring_projection")
    private final boolean isRecurringProjection;
    private final LocalDate date;
}
