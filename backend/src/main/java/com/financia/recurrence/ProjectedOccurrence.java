package com.financia.recurrence;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ProjectedOccurrence {
    private final LocalDate date;
    private final BigDecimal amount;
    private final String description;
    private final boolean cancelled;
    private final boolean modified;
}
