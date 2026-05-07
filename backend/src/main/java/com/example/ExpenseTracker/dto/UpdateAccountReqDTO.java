package com.example.ExpenseTracker.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record UpdateAccountReqDTO(
        @Size(min = 3, max = 30,  message = "{update.username.size}")
        String username,

        @Email(message = "{update.email.format}")
        String email
) {}
