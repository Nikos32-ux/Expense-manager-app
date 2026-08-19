package com.example.ExpenseTracker.dto;

import com.example.ExpenseTracker.model.ExpenseCategory;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseResDTO(
        @Schema(
                description = "Expense id",
                example = "1"
        )
        Long id,
        @Schema(
                description = "Expense amount",
                example = "100.99"
        )
        BigDecimal amount,
        @Schema(
                description = "Expense description",
                example = "Groceries from farmers market"
        )
        String description,
        @Schema(
                description = "Expense creation time in LocalDateTime format ",
                example = "2026-8-8T12:00:00"
        )
        LocalDateTime date,
        @Schema(
                description = "Expense payment method",
                example = "Cash"
        )
        String payment,
        @Schema(
                description = "Expense category id",
                example = "2"
        )
        Long categoryId,
        @Schema(
                description = "Expense category name",
                example = "Groceries"
        )
        String categoryName
) {}