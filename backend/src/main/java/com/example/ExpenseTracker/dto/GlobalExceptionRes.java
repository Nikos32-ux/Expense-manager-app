package com.example.ExpenseTracker.dto;
import lombok.Getter;
import java.time.LocalDate;
import java.time.LocalDateTime;


public record GlobalExceptionRes<T>(
        Integer status,
        T message,
        LocalDate localDate
) {}
