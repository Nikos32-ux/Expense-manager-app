package com.example.ExpenseTracker.exception;

public class CloudinaryException extends RuntimeException{
    String status;
    public CloudinaryException(String message){
        super(message);
    }
}
