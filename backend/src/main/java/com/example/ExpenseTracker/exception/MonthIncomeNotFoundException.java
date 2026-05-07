package com.example.ExpenseTracker.exception;

public class MonthIncomeNotFoundException extends RuntimeException{
    public MonthIncomeNotFoundException(String message){
        super(message);
    }
}
