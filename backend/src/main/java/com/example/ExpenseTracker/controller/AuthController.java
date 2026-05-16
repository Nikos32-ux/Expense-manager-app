package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.service.Auth.AuthService;
import com.example.ExpenseTracker.util.UserContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;


@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final AuthenticationManager authenticationManager;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
   
    @PostMapping("/register")
    public ResponseEntity<RegisterResDTO> userRegister(@Valid @ModelAttribute RegisterRequestDTO registerRequestDTO){
        RegisterResDTO registerResponse = authService.saveUser(registerRequestDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(registerResponse);
    }

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

    @GetMapping("/user-verify")
    public ResponseEntity<LoginResDTO> verifyToken(Authentication auth) {
        logger.info("Received request for user verification for email: {}", auth.getName());
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        LoginResDTO verifiedUserDTO = new LoginResDTO(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getImageProfile()
        );
        return ResponseEntity.ok().body(verifiedUserDTO);
    };

    @PutMapping("update-account-info")
    public ResponseEntity<UpdateAccountResDTO> changeUserInfo(@Valid @RequestBody UpdateAccountReqDTO updateAccount){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        UpdateAccountResDTO updatedAccount = authService.changeAccountInfo(userId, updateAccount);
        return ResponseEntity.ok().body(updatedAccount);
    }

    @PutMapping("update-password")
    public ResponseEntity<UpdatePasswordResDTO> changeUserPassword(@Valid @RequestBody UpdatePasswordReqDTO updatePassword){
        logger.info("NEW password {}", updatePassword.password());
        UserPrincipal user = UserContextUtils.getAuthenticatedUser();
        Long userId = user.getId();
        UpdatePasswordResDTO updatedPassword = authService.changePassword(userId, updatePassword);
        return ResponseEntity.ok(updatedPassword);
    }
}
