package com.example.ExpenseTracker.dto;

public record AdminOverviewResponse(
        long totalUsers,
        long totalExpenses
) {}
