package com.example.ExpenseTracker.mapper;

import com.example.ExpenseTracker.dto.LoginResDTO;
import com.example.ExpenseTracker.model.User;

public final class AuthLoginResMapper {

     public static LoginResDTO resUserDTO(User user){
         return new LoginResDTO(
                 user.getId(),
                 user.getUsername(),
                 user.getEmail(),
                 user.getImageProfile()
         );
     }
}
