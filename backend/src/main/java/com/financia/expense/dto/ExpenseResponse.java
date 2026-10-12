package com.financia.expense.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
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
    private final Long expenseCategoryId;
    private final String paymentMethod;
    private final Long paymentMethodId;
    private final PaymentStatus paymentStatus;
    private final String description;
    @JsonProperty("is_recurring")
    private final boolean isRecurring;

    public static ExpenseResponse from(ExpenseEntry entry) {
        return new ExpenseResponse(
                entry.getId(),
                entry.getDate(),
                entry.getAmount(),
                entry.getCategoryNameSnapshot(),
                entry.getExpenseCategory() != null ? entry.getExpenseCategory().getId() : null,
                entry.getPaymentMethodNameSnapshot(),
                entry.getPaymentMethod() != null ? entry.getPaymentMethod().getId() : null,
                entry.getPaymentStatus(),
                entry.getDescription(),
                entry.getRecurrenceRule() != null
        );
    }
}
