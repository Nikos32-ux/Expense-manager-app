package com.example.ExpenseTracker.dto;
import com.example.ExpenseTracker.model.IncomeCategory;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;


public record AddIncomeRequestDTO(
        @NotNull(message = "{income.amount.notNull}")
        @Positive(message = "{income.amount.positive}")
        @Digits(integer = 12, fraction = 2)
        @Schema(
                description = "Income Amount",
                example = "500.99"
        )
        BigDecimal amount,
        @NotNull(message = "{income.source.notNull}")
        @Schema(
                description = "Income source",
                example = "SALARY"
        )
        IncomeCategory source,


        @NotNull(message = "{income.date.notNull}")
        @PastOrPresent(message = "{income.date.time}")
        @Schema(
                description = "Income creation date.",
                example = "2026-08-08"
        )
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate date
) {}
