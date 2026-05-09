package com.example.ExpenseTracker.dto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record CategoryFilterDTO(

        @NotBlank(message = "{expense.filter.notNull}")
        @Pattern(regexp = "^(day|week|month)$", message = "{expense.filter.invalid}")
         String filter
        ){}
