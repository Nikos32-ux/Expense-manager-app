package com.example.ExpenseTracker.dto;

import com.example.ExpenseTracker.model.ExpenseCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ExpenseResDTO(
        Long id,
        BigDecimal amount,
        String description,
        LocalDateTime date,
        String payment,
        Long categoryId,
        String categoryName
) {}