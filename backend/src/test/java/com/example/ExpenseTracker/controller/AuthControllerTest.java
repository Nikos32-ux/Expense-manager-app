package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.config.jwtConfig.JwtEntryPoint;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.exception.EmailAlreadyExistsException;
import com.example.ExpenseTracker.exception.InvalidAccountUpdateException;
import com.example.ExpenseTracker.exception.InvalidFileTypeException;
import com.example.ExpenseTracker.exception.UserNotFoundException;
import com.example.ExpenseTracker.model.ExpenseCategory;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.service.Auth.AuthService;
import com.example.ExpenseTracker.service.JwtFilterService;
import com.example.ExpenseTracker.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.authentication.AuthenticationManager;

import java.util.HashSet;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.log;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;


@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
public class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    AuthService authService;

    @MockitoBean
    RateLimitingService rateLimitingService;

    @MockitoBean
    JwtEntryPoint jwtEntryPoint;

    @MockitoBean
    JwtFilterService jwtFilterService;

    @MockitoBean
    AuthenticationManager authenticationManager;

    @MockitoBean
    BCryptPasswordEncoder bCryptPasswordEncoder;



    @Nested
    class register{

        @Test
        void shouldReturnStatusCreated_whenRequestDataIsValid() throws Exception{
            RegisterResDTO registerResDTO = new RegisterResDTO("Registration was successful");

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "imageProfile",
                    "file.jpeg",
                    "image/jpeg",
                    "test content".getBytes()
            );

            RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(
                    "TestRegister",
                    "test@gmail.com",
                    "TestRegister1!",
                    mockMultipartFile

            );

            given(authService.saveUser(registerRequestDTO))
                    .willReturn(registerResDTO);

            mockMvc.perform(multipart("/auth/register")
                            .file(mockMultipartFile)
                            .param("username","TestRegister")
                            .param("email", "test@gmail.com")
                            .param("password", "TestRegister1!"))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.message").value("Registration was successful"));

            verify(authService).saveUser(registerRequestDTO);
        }

        @Test
        void shouldReturnConflict_whenEmailAlreadyExists() throws Exception{

            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "imageProfile",
                    "file.jpeg",
                    "image/jpeg",
                    "test content".getBytes()
            );

            RegisterRequestDTO registerRequestDTO = new RegisterRequestDTO(
                    "TestRegister",
                    "test@gmail.com",
                    "TestRegister1!",
                    mockMultipartFile

            );

            given(authService.saveUser(registerRequestDTO))
                    .willThrow(new EmailAlreadyExistsException(registerRequestDTO.email()));

            mockMvc.perform(multipart("/auth/register")
                            .file(mockMultipartFile)
                            .param("username","TestRegister")
                            .param("email", "test@gmail.com")
                            .param("password", "TestRegister1!"))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.status").value(409))
                    .andExpect(jsonPath("$.message.email").value("Email already exists"));

            verify(authService).saveUser(registerRequestDTO);
        }

        @Test
        void shouldReturnBadRequest_whenValidationFails() throws Exception{
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "imageProfile",
                    "file.jpeg",
                    "image/jpeg",
                    "test content".getBytes()
            );


            mockMvc.perform(multipart("/auth/register")
                            .file(mockMultipartFile)
                            .param("username","test")
                            .param("email", "test@gmail.com")
                            .param("password", "TestRegister"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.username").value("Username should be at least 6 characters long and up to 20 maximum"))
                    .andExpect(jsonPath("$.message.password").value("Password must include uppercase, lowercase, number, and special character (8 - 25 characters)"));

            verifyNoInteractions(authService);

        }

        @Test
        void shouldReturnBadRequest_whenInvalidFileType() throws Exception{
            MockMultipartFile mockMultipartFile = new MockMultipartFile(
                    "imageProfile",
                    "file.jpeg",
                    "image/jpeg",
                    "test content".getBytes()
            );

            given(authService.saveUser(any(RegisterRequestDTO.class)))
                    .willThrow(new InvalidFileTypeException("Invalid file type only images are accepted"));

            mockMvc.perform(multipart("/auth/register")
                            .file(mockMultipartFile)
                            .param("username","TestRegister")
                            .param("email", "test@gmail.com")
                            .param("password", "TestRegister1!"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.imageProfile").value("Invalid file type only images are accepted"));

            verify(authService).saveUser(any(RegisterRequestDTO.class));
        }
    }

    @Nested
    class login{

        @Test
        void shouldReturnLoggedInUser_whenValidRequestData() throws Exception{
            String jwt = "jwt-123";

            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                     "test123@gmail.com",
                     "TestDev123!"
            );


            LoginResDTO loginResDTO = new LoginResDTO(
                    1L,
                    "TestDev!",
                    "test123@gmail.com",
                    "file-123"
            );


            WrapperLoginResDTO wrapperLoginResDTO = new WrapperLoginResDTO(loginResDTO, jwt);

            given(authService.userLogin(loginRequestDTO))
                    .willReturn(wrapperLoginResDTO);

            mockMvc.perform(post("/auth/login")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("""
                            {
                                "email": "test123@gmail.com",
                                "password": "TestDev123!"
                            }
                            """)
            )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("TestDev!"))
                    .andExpect(jsonPath("$.email").value("test123@gmail.com"))
                    .andExpect(jsonPath("$.imageProfile").value("file-123"))
                    .andExpect(header().string("Set-Cookie", containsString("jwt-123")));


            verify(authService).userLogin(loginRequestDTO);
        }

        @Test
        void shouldReturnBadRequest_whenEmailIsMissing() throws Exception{

            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "password": "TestDev123!"
                            }
                            """)
                    )
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.email").value("Please enter your email"));


            verify(authService, never()).userLogin(any(LoginRequestDTO.class));
        }

        @Test
        void shouldReturnUnauthorized_whenCredentialsAreInvalid() throws Exception{
            LoginRequestDTO loginRequestDTO = new LoginRequestDTO(
                    "test123@gmail.com",
                    "TestDev123!"
            );

            given(authService.userLogin(loginRequestDTO))
                    .willThrow(new BadCredentialsException("Bad Credentials"));


            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("""
                            {
                                "email": "test123@gmail.com",
                                "password": "TestDev123!"
                            }
                            """)
                    )
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.status").value(401))
                    .andExpect(jsonPath("$.message").value("Invalid credentials"));


            verify(authService).userLogin(any(LoginRequestDTO.class));
        }
    }

    @Nested
    class logout{

        @Test
        void shouldClearJwt_whenLogoutIsRequested() throws Exception{

            mockMvc.perform(post("/auth/logout"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Successfully logged out"))
                    .andExpect(header().string("Set-Cookie", containsString("jwtToken")))
                    .andExpect(header().string("Set-Cookie", containsString("Max-Age=0")));
        }
    }

    @Nested
    class verifyToken{

        @Test
        void shouldReturnUserDTO_WhenAuthenticated() throws Exception{
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("Test");
            mockUser.setEmail("test@gmail.com");
            mockUser.setImageProfile("file-124");
            mockUser.setRoles(new HashSet<>());


            UserPrincipal userPrincipal = new UserPrincipal(mockUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            mockMvc.perform(get("/auth/user-verify"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.username").value("Test"))
                    .andExpect(jsonPath("$.email").value("test@gmail.com"))
                    .andExpect(jsonPath("$.imageProfile").value("file-124"));
        }
    }

    @Nested
    class updateAccountInfo{

        @Test
        void shouldReturnUpdatedAccount_whenValidRequestData() throws Exception{
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("Test");
            mockUser.setEmail("test@gmail.com");
            mockUser.setImageProfile("file-124");
            mockUser.setRoles(new HashSet<>());


            UserPrincipal userPrincipal = new UserPrincipal(mockUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    "NewName!",
                    "newEmail@gmail.com"
            );

            UpdateAccountResDTO updateAccountResDTO = new UpdateAccountResDTO(
                    "NewName!",
                    "newEmail@gmail.com",
                    "Updated account successfully"
            );

            given(authService.changeAccountInfo(mockUser.getId(), updateAccountReqDTO))
                    .willReturn(updateAccountResDTO);

            mockMvc.perform(put("/auth/update-account-info")
                    .accept(MediaType.APPLICATION_JSON)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                               {
                                   "username": "NewName!",
                                   "email": "newEmail@gmail.com"
                               }
                            """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.username").value("NewName!"))
                    .andExpect(jsonPath("$.email").value("newEmail@gmail.com"))
                    .andExpect(jsonPath("$.message").value("Updated account successfully"));

            verify(authService).changeAccountInfo(mockUser.getId(), updateAccountReqDTO);
        }

        @Test
        void shouldReturnBadRequest_whenValidationRequestBodyFails() throws Exception{
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("Test");
            mockUser.setEmail("test@gmail.com");
            mockUser.setImageProfile("file-124");
            mockUser.setRoles(new HashSet<>());


            UserPrincipal userPrincipal = new UserPrincipal(mockUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    "New!",
                    "newEmail@gmail.com"
            );

            mockMvc.perform(put("/auth/update-account-info")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                               {
                                   "username": "New!",
                                   "email": "newEmail@gmail.com"
                               }
                            """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.username").value("Username must be between 6 and 20 characters"));

            verify(authService, never()).changeAccountInfo(mockUser.getId(), updateAccountReqDTO);
        }

        @Test
        void shouldReturnNotFound_whenUserDoesNotExist() throws Exception{
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("Test");
            mockUser.setEmail("test@gmail.com");
            mockUser.setImageProfile("file-124");
            mockUser.setRoles(new HashSet<>());


            UserPrincipal userPrincipal = new UserPrincipal(mockUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    "NewName!",
                    "newEmail@gmail.com"
            );

            given(authService.changeAccountInfo(mockUser.getId(), updateAccountReqDTO))
                    .willThrow(new UserNotFoundException("User not found"));

            mockMvc.perform(put("/auth/update-account-info")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                               {
                                   "username": "NewName!",
                                   "email": "newEmail@gmail.com"
                               }
                            """))
                            .andExpect(status().isNotFound())
                                    .andExpect(jsonPath("$.status").value(404))
                                    .andExpect(jsonPath("$.message").value("User not found"));



            verify(authService).changeAccountInfo(mockUser.getId(), updateAccountReqDTO);
        }

        @Test
        void shouldReturnBadRequest_whenNoRequestDataAreProvided() throws Exception{
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("Test");
            mockUser.setEmail("test@gmail.com");
            mockUser.setImageProfile("file-124");
            mockUser.setRoles(new HashSet<>());


            UserPrincipal userPrincipal = new UserPrincipal(mockUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    null,
                    null
            );

            given(authService.changeAccountInfo(mockUser.getId(), updateAccountReqDTO))
                    .willThrow(new InvalidAccountUpdateException("Either email or username must be provided"));

            mockMvc.perform(put("/auth/update-account-info")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                                       {
                                          "username": null,
                                          "email": null
                                       }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message").value("Either email or username must be provided"));



            verify(authService).changeAccountInfo(mockUser.getId(), updateAccountReqDTO);
        }
    }

    @Nested
    class updatePassword{

        @Test
        void shouldReturnSuccess_whenPasswordIsUpdated() throws Exception{
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("Test");
            mockUser.setEmail("test@gmail.com");
            mockUser.setImageProfile("file-124");
            mockUser.setRoles(new HashSet<>());


            UserPrincipal userPrincipal = new UserPrincipal(mockUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UpdatePasswordReqDTO updatePasswordReqDTO = new UpdatePasswordReqDTO("UpdatedPass1!");
            UpdatePasswordResDTO updatePasswordResDTO = new UpdatePasswordResDTO("success", "Updated password successfully");

            given(authService.changePassword(mockUser.getId(), updatePasswordReqDTO))
                    .willReturn(updatePasswordResDTO);

            mockMvc.perform(put("/auth/update-password")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("""
                           {
                               "password": "UpdatedPass1!"
                           }
                           """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("success"))
                    .andExpect(jsonPath("$.message").value("Updated password successfully"));

            verify(authService).changePassword(mockUser.getId(), updatePasswordReqDTO);
        }

        @Test
        void shouldReturnNotFound_whenUserDoesNotExist() throws Exception{
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("Test");
            mockUser.setEmail("test@gmail.com");
            mockUser.setImageProfile("file-124");
            mockUser.setRoles(new HashSet<>());


            UserPrincipal userPrincipal = new UserPrincipal(mockUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            UpdatePasswordReqDTO updatePasswordReqDTO = new UpdatePasswordReqDTO("NewPass123!");

            given(authService.changePassword(mockUser.getId(), updatePasswordReqDTO))
                    .willThrow(new UserNotFoundException("User not found"));

            mockMvc.perform(put("/auth/update-password")
                            .accept(MediaType.APPLICATION_JSON)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                               {
                                   "password": "NewPass123!"
                               }
                            """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("User not found"));



            verify(authService).changePassword(mockUser.getId(), updatePasswordReqDTO);
        }

        @Test
        void shouldReturnBadRequest_whenPasswordHasInvalidPattern() throws Exception{
            User mockUser = new User();
            mockUser.setId(1L);
            mockUser.setUsername("Test");
            mockUser.setEmail("test@gmail.com");
            mockUser.setImageProfile("file-124");
            mockUser.setRoles(new HashSet<>());


            UserPrincipal userPrincipal = new UserPrincipal(mockUser);
            Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

            mockMvc.perform(put("/auth/update-password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("""
                           {
                               "password": "UpdatedPass"
                           }
                           """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.password").value("Password must include uppercase, lowercase, number, and special character (8 - 25 characters)"));

            verify(authService, never()).changePassword(anyLong(), any(UpdatePasswordReqDTO.class));
        }
    }
}
