package com.financia.auth.dto;

import com.financia.user.ThemePreference;
import com.financia.user.User;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserResponse {
    private final Long id;
    private final String name;
    private final String email;
    private final ThemePreference themePreference;

    public static UserResponse from(User user) {
        return new UserResponse(user.getId(), user.getName(), user.getEmail(), user.getThemePreference());
    }
}
