package com.example.ExpenseTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;

public record ExpenseReqDTO(

        @NotNull(message = "{expense.amount.notnull}")
        @Positive(message = "{expense.amount.positive}")
        @Schema(
                description = "Expense amount",
                example = "100.99"
        )
        BigDecimal amount,

        @NotNull(message = "{expense.category.notnull}")
        @Schema(
                description = "Expense's category id",
                example = "1"
        )
        Long categoryId,

        @NotBlank(message = "{expense.description.notBlank}")
        @Size(min = 1, max = 255, message = "{expense.description.size}")
        @Schema(
                description = "Client description of created expense",
                example = "month"
        )
        String description,

        @NotNull(message = "{expense.date.notnull}")
        @Schema(
                description = "Date of expense creation",
                example = "2026-8-8"
        )
        LocalDate date,

        @NotNull(message = "{expense.time.notnull}")
        @Schema(
                description = "Time of expense creation",
                example = "12:00:00"
        )
        LocalTime time,

        @NotBlank(message = "{expense.payment.notBlank}")
        @Pattern(regexp = "cash|card", message = "{expense.payment.pattern}")
        @Schema(
                description = "Payment method",
                example = "Card",
                allowableValues = {"cash", "card"}
        )
        String payment
) {}