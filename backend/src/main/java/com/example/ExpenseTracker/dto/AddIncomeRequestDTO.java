package com.example.ExpenseTracker.dto;


import com.example.ExpenseTracker.model.IncomeCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record AddIncomeRequestDTO(
        @NotNull(message = "{income.amount.notNull}")
        @Positive(message = "{income.amount.positive}")
        @Digits(integer = 12, fraction = 2)
        BigDecimal amount,
        @NotNull(message = "{income.source.notNull}")
        IncomeCategory source,
        @NotNull(message = "{income.date.notNull}")
        @PastOrPresent(message = "{income.date.time}")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date
) {}
