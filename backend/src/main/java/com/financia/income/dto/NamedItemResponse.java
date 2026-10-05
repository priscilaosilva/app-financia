package com.financia.income.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class NamedItemResponse {
    private final Long id;
    private final String name;
}
