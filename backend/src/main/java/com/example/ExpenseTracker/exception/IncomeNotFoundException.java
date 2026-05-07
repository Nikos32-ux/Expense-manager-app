package com.example.ExpenseTracker.exception;

public class IncomeNotFoundException extends RuntimeException{
    public IncomeNotFoundException(String message){
        super(message);
    }
}
