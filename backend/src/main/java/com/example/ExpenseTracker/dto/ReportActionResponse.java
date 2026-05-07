package com.example.ExpenseTracker.dto;

import java.time.LocalDateTime;

public record ReportActionResponse(
        String status,
        String csvFilePath,
        LocalDateTime sentAt
) {}
