package com.example.ExpenseTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateAccountReqDTO(
        @Size(min = 6, max = 20,  message = "{update.username.size}")
        @Schema(description = "Username for login", example = "TestDev")
        String username,

        @Email(message = "{update.email.format}")
        @Schema(description = "Email for login", example = "test@gmail.com")
        String email
) {}
