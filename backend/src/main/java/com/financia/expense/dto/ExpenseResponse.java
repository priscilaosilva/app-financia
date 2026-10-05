package com.financia.expense.dto;

import com.financia.expense.ExpenseEntry;
import com.financia.expense.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class ExpenseResponse {
    private final Long id;
    private final LocalDate date;
    private final BigDecimal amount;
    private final String category;
    private final String paymentMethod;
    private final PaymentStatus paymentStatus;
    private final String description;

    public static ExpenseResponse from(ExpenseEntry entry) {
        return new ExpenseResponse(
                entry.getId(),
                entry.getDate(),
                entry.getAmount(),
                entry.getCategoryNameSnapshot(),
                entry.getPaymentMethodNameSnapshot(),
                entry.getPaymentStatus(),
                entry.getDescription()
        );
    }
}
