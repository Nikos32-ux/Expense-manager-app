package com.example.ExpenseTracker.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public interface ExpenseReportDTO {
    BigDecimal getAmount();
    String getDescription();
    LocalDateTime getDate();
    String getMonth();
    Integer getYear();
    String getPayment();
    String getCategory();
    String getExpenseLevel();
}
