package com.financia.paymentmethod.dto;

import com.financia.paymentmethod.PaymentMethod;
import com.financia.paymentmethod.PaymentMethodType;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentMethodResponse {
    private final Long id;
    private final String name;
    private final PaymentMethodType type;
    private final Integer dueDay;
    private final Integer billingStartDay;
    private final Integer billingEndDay;

    public static PaymentMethodResponse from(PaymentMethod method) {
        return new PaymentMethodResponse(
                method.getId(),
                method.getName(),
                method.getType(),
                method.getDueDay(),
                method.getBillingStartDay(),
                method.getBillingEndDay()
        );
    }
}
