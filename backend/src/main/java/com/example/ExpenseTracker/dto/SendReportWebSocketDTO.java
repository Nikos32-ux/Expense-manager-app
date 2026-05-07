package com.example.ExpenseTracker.dto;

import java.time.LocalDateTime;

public record SendReportWebSocketDTO(
        Long id,
        LocalDateTime sentAt,
        String csvFile,
        String type,
        boolean isRead
) {}
