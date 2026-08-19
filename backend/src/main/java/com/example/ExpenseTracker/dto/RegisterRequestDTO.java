package com.example.ExpenseTracker.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import org.springframework.web.multipart.MultipartFile;


public record RegisterRequestDTO(
        @NotBlank(message="{register.username.notBlank}")
        @Size(min=6, max=20, message="{register.username.size}")
        @Schema(
                description = "Username for the account.",
                example = "TestDev"
        )
        String username,

        @NotBlank(message ="{register.email.notBlank}" )
        @Email(message = "{register.email.properFormat}")
        @Schema(
                description = "Email.",
                example = "test@gmail.com"
        )
        String email,

        @Pattern(
                regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,25}$",
                message = "{register.password.pattern}"
        )
        @NotBlank(message="{register.password.notBlank}")
        @Schema(
                description = "Password for the account. Must contain at least one uppercase letter, one lowercase letter, one number, and one special character",
                example = "TestDev1!"
        )
        String password,

        @NotNull(message = "{register.imageProfile.notNull}")
        @Schema(
                description = "Profile image to upload",
                type = "string",
                format = "binary"
        )
        MultipartFile imageProfile
) {}
