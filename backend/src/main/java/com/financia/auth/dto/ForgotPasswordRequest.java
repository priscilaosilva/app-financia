package com.financia.auth.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ForgotPasswordRequest {
    @NotBlank
    @Email
    private String email;

    @NotBlank
    @Size(min = 6)
    private String newPassword;

    @NotBlank
    @Size(min = 6)
    private String newPasswordConfirmation;

    @AssertTrue(message = "A confirmação deve ser igual à senha nova")
    public boolean isPasswordConfirmed() {
        if (newPassword == null || newPasswordConfirmation == null) {
            return true;
        }
        return newPassword.equals(newPasswordConfirmation);
    }
}
