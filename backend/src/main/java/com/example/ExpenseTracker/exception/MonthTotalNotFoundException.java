package com.example.ExpenseTracker.exception;

public class MonthTotalNotFoundException extends RuntimeException {
    public MonthTotalNotFoundException(String message){
        super(message);
    }
}
