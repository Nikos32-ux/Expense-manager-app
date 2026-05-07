package com.example.ExpenseTracker.util;
import com.example.ExpenseTracker.security.UserPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class UserContextUtils {

    public static UserPrincipal getAuthenticatedUser(){
        Authentication authContext = SecurityContextHolder.getContext().getAuthentication();
        return (UserPrincipal) authContext.getPrincipal();
    }
}
