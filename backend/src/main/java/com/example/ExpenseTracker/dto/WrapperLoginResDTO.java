package com.example.ExpenseTracker.dto;



public record WrapperLoginResDTO(
        LoginResDTO user,
        String token
) {}
