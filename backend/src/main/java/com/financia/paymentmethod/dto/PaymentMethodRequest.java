package com.financia.paymentmethod.dto;

import com.financia.paymentmethod.PaymentMethodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentMethodRequest {
    @NotBlank
    @Size(max = 100)
    private String name;

    @NotNull
    private PaymentMethodType type;

    private Integer dueDay;
    private Integer billingStartDay;
    private Integer billingEndDay;
}
