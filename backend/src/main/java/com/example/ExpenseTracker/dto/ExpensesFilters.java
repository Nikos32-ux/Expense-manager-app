package com.example.ExpenseTracker.dto;

public record ExpensesFilters (
      String month,
      String category,
      Integer amountMin,
      Integer amountMax,
      String search
    ){}
