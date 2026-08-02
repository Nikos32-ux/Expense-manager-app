package com.example.ExpenseTracker.dto;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ExpensesFilters (
      @Pattern(
              regexp = "(?i)^(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)$",
              message = "{validation.filters.month.invalid}"
      )
      String month,
      String category,

      @PositiveOrZero(message = "{validation.filters.amountMin.positive}")
      Integer amountMin,

      @PositiveOrZero(message = "{validation.filters.amountMax.positive}")
      Integer amountMax,

      @Size(max = 50, message = "{validation.filters.search.maxSize}")
      String search
    ){}
