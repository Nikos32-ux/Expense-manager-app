package com.example.ExpenseTracker.dto;

import com.example.ExpenseTracker.model.IncomeCategory;

import java.math.BigDecimal;
import java.time.LocalDate;

public record IncomeDataDTO(
        Long id,
        BigDecimal amount,
        IncomeCategory source,
        LocalDate date
) {
}
