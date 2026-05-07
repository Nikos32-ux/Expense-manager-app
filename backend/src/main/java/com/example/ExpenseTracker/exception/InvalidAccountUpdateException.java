package com.example.ExpenseTracker.exception;

public class InvalidAccountUpdateException extends RuntimeException{

  public InvalidAccountUpdateException(String message){
        super(message);
    }
}
