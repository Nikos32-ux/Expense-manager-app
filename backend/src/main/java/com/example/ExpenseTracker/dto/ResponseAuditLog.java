package com.example.ExpenseTracker.dto;

import java.time.LocalDateTime;

public interface ResponseAuditLog{
    String getAction();
    String getEmail();
    LocalDateTime getTimestamp();
}
