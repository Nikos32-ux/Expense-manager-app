package com.example.ExpenseTracker.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

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
        String description,

        @NotNull(message = "{expense.date.notnull}")
        LocalDate date,

        @NotNull(message = "{expense.time.notnull}")
        LocalTime time,

        @NotBlank(message = "{expense.payment.notBlank}")
        String payment
) {}