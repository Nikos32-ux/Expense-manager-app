package com.example.ExpenseTracker.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdatePasswordReqDTO(

        @NotBlank(message = "{update.password.notBlank}"    )
        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,25}$",
                message = "{update.password.pattern}"
        )
        @Schema(description = "Password with mixed case ,numbers and symbols", example = "TestDev1!")
        String password
) {
}
