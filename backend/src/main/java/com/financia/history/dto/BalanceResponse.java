package com.financia.history.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class BalanceResponse {
    private final BigDecimal balance;
    private final LocalDate calculatedAt;
}
