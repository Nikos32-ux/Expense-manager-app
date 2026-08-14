package com.example.ExpenseTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateAccountReqDTO(
        @Size(min = 6, max = 20,  message = "{update.username.size}")
        String username,

        @Email(message = "{update.email.format}")
        String email
) {}
