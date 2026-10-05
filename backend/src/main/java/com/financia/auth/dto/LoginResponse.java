package com.financia.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class LoginResponse {
    private final String token;
    private final Instant expiresAt;
}
