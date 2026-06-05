package com.example.ExpenseTracker.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordReqDTO(

        @NotBlank(message = "{update.password.notBlank}"    )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[@$!%*?&])[A-Za-z@$!%*?&]{8,25}$",
                message = "{update.password.pattern}"
        )
        String password
) {
}
