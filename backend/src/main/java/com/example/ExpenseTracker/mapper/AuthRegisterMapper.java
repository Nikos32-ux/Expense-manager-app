package com.example.ExpenseTracker.mapper;
import com.example.ExpenseTracker.dto.RegisterRequestDTO;
import com.example.ExpenseTracker.model.User;

public final class AuthRegisterMapper {

    private AuthRegisterMapper(){};

    public static User toEntity(RegisterRequestDTO registerResponseDTO){
        User user = new User();

        user.setUsername(registerResponseDTO.username());
        user.setPassword(registerResponseDTO.password());
        user.setEmail(registerResponseDTO.email());

        return user;
    }
}
