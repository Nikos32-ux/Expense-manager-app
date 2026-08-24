package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.service.Auth.AuthService;
import com.example.ExpenseTracker.util.UserContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Auth management endpoints")

public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user using registration details client provides via multipart/form-data."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User successfully registered."),
            @ApiResponse(responseCode = "400", description = "Validation error, invalid image file type, or upload error."),
            @ApiResponse(responseCode = "409", description = "Email already exists or database constraint violation."),
            @ApiResponse(responseCode = "413", description = "Uploaded file is too large (max: 5MB).")
    })

    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<RegisterResDTO> userRegister(@Valid @ModelAttribute RegisterRequestDTO registerRequestDTO){
        RegisterResDTO registerResponse = authService.saveUser(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

    @Operation(
            summary = "Login",
            description = "Login providing login details via request payload."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User successfully logged in."),
            @ApiResponse(responseCode = "400", description = "Validation error."),
            @ApiResponse(responseCode = "401", description = "Invalid Credentials."),
    })
    @PostMapping("/login")
    public ResponseEntity<LoginResDTO> login(@Valid @RequestBody LoginRequestDTO loginRequestDTO) {
        WrapperLoginResDTO result = authService.userLogin(loginRequestDTO);

        ResponseCookie resCookie = ResponseCookie.from("jwtToken", result.token())
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(10 * 60 *60)
                .sameSite("Lax")
                .build();

        return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, resCookie.toString())
                .body(result.user());
    }

    @Operation(
            summary = "Logout",
            description = "Logout by clearing the authentication cookie."
    )
    @ApiResponse(responseCode = "200", description = "User successfully logged out.")
    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie logoutCookie = ResponseCookie.from("jwtToken", "")
                .maxAge(0)
                .path("/")
                .httpOnly(true)
                .secure(false)
                .sameSite("Lax")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, logoutCookie.toString())
                .body("Successfully logged out");
    }

    @Operation(
            summary = "User verification.",
            description = "Verifies user by validating jwt and returns profile details of authenticated user."
    )
    @ApiResponse(responseCode = "200", description = "Valid user, user details successfully retrieved.")
    @GetMapping("/user-verify")
    public ResponseEntity<LoginResDTO> verifyToken() {
        UserPrincipal user = UserContextUtils.getAuthenticatedUser();
        logger.info("Received request for user verification for email: {}", user.getDisplayName());
        LoginResDTO verifiedUserDTO = new LoginResDTO(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getImageProfile(),
                user.getRole()
        );
        return ResponseEntity.ok().body(verifiedUserDTO);
    };

    @Operation(
            summary = "Update user account.",
            description = "Updates user account details username and email."
    )
    @ApiResponse(responseCode = "200", description = "Account details successfully updated.")
    @ApiResponse(responseCode = "400", description = "Validation error.")
    @ApiResponse(responseCode = "404", description = "User was not found in the system.")
    @PutMapping("update-account-info")
    public ResponseEntity<UpdateAccountResDTO> changeUserInfo(@Valid @RequestBody UpdateAccountReqDTO updateAccount){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        UpdateAccountResDTO updatedAccount = authService.changeAccountInfo(userId, updateAccount);
        return ResponseEntity.ok().body(updatedAccount);
    }

    @Operation(
            summary = "Update user password.",
            description = "Updates user account password."
    )
    @ApiResponse(responseCode = "200", description = "Account password successfully updated .")
    @ApiResponse(responseCode = "400", description = "Validation error(password does not match requirements or is blank).")
    @ApiResponse(responseCode = "404", description = "User was not found in the system.")
    @PutMapping("update-password")
    public ResponseEntity<UpdatePasswordResDTO> changeUserPassword(@Valid @RequestBody UpdatePasswordReqDTO updatePassword){
        logger.info("NEW password {}", updatePassword.password());
        UserPrincipal user = UserContextUtils.getAuthenticatedUser();
        Long userId = user.getId();
        UpdatePasswordResDTO updatedPassword = authService.changePassword(userId, updatePassword);
        return ResponseEntity.ok(updatedPassword);
    }
}
