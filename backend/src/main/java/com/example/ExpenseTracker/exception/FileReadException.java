package com.example.ExpenseTracker.exception;

public class FileReadException extends RuntimeException{
    public FileReadException(String message, Throwable cause){
        super(message);
    }
}
