package com.example.ExpenseTracker.dto;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;


public record ExpensesFilters (
      String month,
      String category,
      Integer amountMin,
      Integer amountMax,
      String search
    ){}
