package com.financia.auth;

import lombok.Getter;
import org.springframework.security.core.AuthenticatedPrincipal;

@Getter
public class UserPrincipal implements AuthenticatedPrincipal {
    private final Long userId;
    private final String email;

    public UserPrincipal(Long userId, String email) {
        this.userId = userId;
        this.email = email;
    }

    @Override
    public String getName() {
        return email;
    }
}
