package com.example.ExpenseTracker.dto;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;


public record RegisterRequestDTO(
        @NotBlank(message="{register.username.notBlank}")
        @Size(min=3, max=10, message="{register.username.size}")
        String username,

        @NotBlank(message ="{register.email.notBlank}" )
        @Email(message = "{register.email.properFormat}")
        String email,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,12}$",
                message = "{register.password.pattern}"
        )
        @NotBlank(message="{register.password.notBlank}")
        String password,

        MultipartFile imageProfile
) {}
