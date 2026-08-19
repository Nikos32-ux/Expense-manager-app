package com.example.ExpenseTracker.dto;
import io.swagger.v3.oas.annotations.media.Schema;

public record AddExpenseResDTO(
        @Schema(
                description = "Response status of created expense",
                example = "201"
        )
        String status
) {}
