package com.example.ExpenseTracker.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryFilterDTO(

        @NotBlank(message = "{expense.filter.notNull}")
        @Pattern(regexp = "^(day|week|month)$", message = "{expense.filter.invalid}")
        @Schema(
                description = "Time period filter to calculate categories totals",
                example = "month"
        )
         String filter
        ){}
