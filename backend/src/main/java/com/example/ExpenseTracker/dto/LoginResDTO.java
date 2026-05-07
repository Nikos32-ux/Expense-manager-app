package com.example.ExpenseTracker.dto;

import java.time.LocalDateTime;

public record LoginResDTO(
        Long id,
        String username,
        String email,
        String imageProfile
) {}
