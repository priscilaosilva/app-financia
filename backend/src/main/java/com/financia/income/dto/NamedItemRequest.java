package com.financia.income.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NamedItemRequest {
    @NotBlank
    @Size(max = 100)
    private String name;
}
