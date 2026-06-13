package com.example.ExpenseTracker.dto;

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
        BigDecimal amount,

        @NotNull(message = "{expense.category.notnull}")
        Long categoryId,

        @NotBlank(message = "{expense.description.notBlank}")
        @Size(min = 1, max = 255, message = "{expense.description.size}")
        String description,

        @NotNull(message = "{expense.date.notnull}")
        LocalDate date,

        @NotNull(message = "{expense.time.notnull}")
        LocalTime time,

        @NotBlank(message = "{expense.payment.notBlank}")
        @Pattern(regexp = "cash|card", message = "{expense.payment.pattern}")
        String payment
) {}