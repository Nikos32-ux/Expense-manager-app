package com.example.ExpenseTracker.exception;


public class EmailAlreadyExistsException extends RuntimeException{

    public EmailAlreadyExistsException(String email){
        super("Email " + email + " already exists, try new one");
    }
}
