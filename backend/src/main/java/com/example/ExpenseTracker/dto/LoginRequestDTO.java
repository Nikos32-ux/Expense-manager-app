package com.example.ExpenseTracker.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;


public record LoginRequestDTO(
        @NotBlank(message="{login.email.notBlank}")
        @Schema(description = "Email for login", example = "test@gmail.com")
        String email,

        @NotBlank(message="{login.password.notBlank}")
        @Schema(description = "Password for login", example = "TestDev1!")
        String password
) {}
