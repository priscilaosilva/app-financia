package com.financia.history.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class HistoryResponse {
    private final LocalDate from;
    private final LocalDate to;
    private final List<HistoryRowDto> rows;
}
