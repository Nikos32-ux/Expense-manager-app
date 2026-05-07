package com.example.ExpenseTracker.rateLimitingConfig;
import org.springframework.security.core.AuthenticationException;

public class RateLimitingException extends AuthenticationException {

    public RateLimitingException(String message){
        super(message);
    }
}
