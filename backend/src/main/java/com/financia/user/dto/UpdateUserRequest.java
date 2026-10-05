package com.financia.user.dto;

import com.financia.user.ThemePreference;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserRequest {
    @Size(max = 100)
    private String name;

    @Email
    private String email;

    private ThemePreference themePreference;
}
