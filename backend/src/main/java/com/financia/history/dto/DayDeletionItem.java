package com.financia.history.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DayDeletionItem {
    @NotNull
    private Long id;

    @NotNull
    private String type;
}
