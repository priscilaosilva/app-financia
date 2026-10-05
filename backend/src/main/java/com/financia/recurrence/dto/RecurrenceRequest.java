package com.financia.recurrence.dto;

import com.financia.recurrence.Frequency;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class RecurrenceRequest {
    private Frequency frequency;
    private LocalDate startDate;
    private LocalDate endDate;
}
