package com.financia.history.dto;

import com.financia.common.RecurrenceScope;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DayDeletionRequest {
    @NotNull
    private LocalDate date;

    @NotEmpty
    @Valid
    private List<DayDeletionItem> items;

    private RecurrenceScope recurrenceScope;
}
