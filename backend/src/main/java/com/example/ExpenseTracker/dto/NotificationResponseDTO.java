package com.example.ExpenseTracker.dto;

import java.time.LocalDateTime;

public record NotificationResponseDTO(
        Long id,
        LocalDateTime sentAt,
        String csv_file,
        String type,
        boolean isRead
) {}
