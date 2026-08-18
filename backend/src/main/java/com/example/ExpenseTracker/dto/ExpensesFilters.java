package com.example.ExpenseTracker.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ExpensesFilters (
      @Pattern(
              regexp = "(?i)^(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER)$",
              message = "{validation.filters.month.invalid}"
      )
      @Schema(
              description = "Filter expenses by month name",
              example = "AUGUST"
      )
      String month,
      String category,

      @PositiveOrZero(message = "{validation.filters.amountMin.positive}")
      @Schema(
              description = "Minimum expense amount filter",
              example = "10"
      )
      Integer amountMin,

      @PositiveOrZero(message = "{validation.filters.amountMax.positive}")
      @Schema(
              description = "Maximum expense amount filter",
              example = "200"
      )
      Integer amountMax,

      @Size(max = 50, message = "{validation.filters.search.maxSize}")
      @Schema(
              description = "Search expenses by matching expense text description",
              example = "Groceries from farmers market"
      )
      String search
    ){}
