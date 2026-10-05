package com.financia.income.dto;

import com.financia.common.RecurrenceScope;
import com.financia.recurrence.dto.RecurrenceRequest;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
public class IncomeRequest {
    @NotNull
    private LocalDate date;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal amount;

    private Long incomeSourceId;
    private String description;
    private RecurrenceRequest recurrence;
    private RecurrenceScope recurrenceScope;
}
