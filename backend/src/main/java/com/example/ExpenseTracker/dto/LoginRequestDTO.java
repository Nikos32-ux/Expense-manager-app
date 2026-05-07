package com.example.ExpenseTracker.dto;
import jakarta.validation.constraints.NotBlank;


public record LoginRequestDTO(
        @NotBlank(message="{login.email.notBlank}")
        String email,

        @NotBlank(message="{login.password.notBlank}")
        String password
) {}
