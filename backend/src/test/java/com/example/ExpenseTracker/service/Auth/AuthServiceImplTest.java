package com.example.ExpenseTracker.service.Auth;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.exception.*;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.example.ExpenseTracker.config.jwtConfig.JwtUtils;
import com.example.ExpenseTracker.model.RoleCategory;
import com.example.ExpenseTracker.model.Roles;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.model.UserActionsCategory;
import com.example.ExpenseTracker.repository.RolesRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.service.CloudinaryService;
import com.example.ExpenseTracker.service.TikaService;
import com.example.ExpenseTracker.service.audit.AuditPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private LoadingCache<String, User> cache;

    @Mock
    RolesRepository rolesRepository;

    @Mock
    private TikaService tikaService;

    @Mock
    private CloudinaryService cloudinaryService;

    @Mock
    private AuditPublisher auditPublisher;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    AuthServiceImpl authService;


    private RegisterRequestDTO registerRequestDTO;
    private LoginRequestDTO loginRequestDTO;
    private Roles role;
    private Authentication authentication;
    private UserPrincipal userPrincipal;
    private UpdatePasswordReqDTO updatePasswordReqDTO;

    private final Long userId = 1L;


    @BeforeEach
    void setUp(){
        registerRequestDTO = new RegisterRequestDTO(
                "Nikos",
                "email@test.com",
                "Kavalas2",
                new MockMultipartFile("imageProfile", "test.jpg", "image/jpeg", new byte[]{1,2,3})

        );

         loginRequestDTO = new LoginRequestDTO(
                "email@test.com",
                "pass"
        );
    }

    @Nested
     class saveUser{
        @Test
        void saveUser_pass(){
            role = new Roles();
            role.setId(1L);
            role.setRoleType(RoleCategory.ROLE_USER);
            doNothing().when(tikaService).validateFile(any());

            when(userRepository.existsByEmail(registerRequestDTO.email()))
                    .thenReturn(false);

            when(rolesRepository.findByRoleType(RoleCategory.ROLE_USER))
                    .thenReturn(Optional.of(role));
            when(cloudinaryService.uploadImageProfile(any(),any()))
                    .thenReturn("fake-url");
            when(passwordEncoder.encode(registerRequestDTO.password()))
                    .thenReturn("hashedPassword");
            when(userRepository.save(any()))
                    .thenAnswer(i -> {
                       User user = i.getArgument(0);
                       user.setId(userId);
                       return user;
                    });

            RegisterResDTO result = authService.saveUser(registerRequestDTO);

            assertEquals( "Registration was successful", result.message());

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            verify(userRepository).save(userCaptor.capture());

            assertEquals("fake-url", userCaptor.getValue().getImageProfile());
            assertEquals(registerRequestDTO.email(),userCaptor.getValue().getEmail());
            assertEquals("hashedPassword", userCaptor.getValue().getPassword());
            assertEquals(registerRequestDTO.username(), userCaptor.getValue().getUsername());
            assertTrue(userCaptor.getValue().getRoles().contains(role));

            verify(tikaService).validateFile(any());
            verify(userRepository).existsByEmail(registerRequestDTO.email());
            verify(cloudinaryService).uploadImageProfile(
                    eq(registerRequestDTO.imageProfile()),
                    eq(registerRequestDTO.email())
            );
            verify(passwordEncoder).encode(registerRequestDTO.password());
            verify(auditPublisher).publishEvent(
                   eq(userId),
                   eq(UserActionsCategory.USER_REGISTERED),
                   eq("USER"),
                    any()
           );
        }

        @Test
        void saveUser_InvalidFileType_throwsException(){
            doThrow(new InvalidFileTypeException("Invalid"))
                    .when(tikaService).validateFile(any());

           assertThrows(InvalidFileTypeException.class, ()->{
               authService.saveUser(registerRequestDTO);
           });

            verify(userRepository, never()).existsByEmail(any());
            verify(userRepository, never()).save(any());
            verify(cloudinaryService, never()).uploadImageProfile(any(), any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
            verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void saveUser_EmailAlreadyExists_throwsException(){
            doNothing().when(tikaService).validateFile(any());

            when(userRepository.existsByEmail(registerRequestDTO.email()))
                    .thenReturn(true);

            assertThrows(EmailAlreadyExistsException.class, () -> {
                authService.saveUser(registerRequestDTO);
            });

            verify(userRepository).existsByEmail(registerRequestDTO.email());
            verify(cloudinaryService, never()).uploadImageProfile(any(), any());
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
            verify(rolesRepository, never()).findByRoleType(any());

        }

        @Test
        void saveUser_CloudinaryFailedUploadImage_throwsException(){
            doNothing().when(tikaService).validateFile(any());
            when(userRepository.existsByEmail(registerRequestDTO.email()))
                    .thenReturn(false);
            when(cloudinaryService.uploadImageProfile(
                    registerRequestDTO.imageProfile(),
                    registerRequestDTO.email())
            ).thenThrow(new CloudinaryException("Cloudinary failed to upload image"));

            assertThrows(CloudinaryException.class, () -> {
                authService.saveUser(registerRequestDTO);
            });


            verify(cloudinaryService).uploadImageProfile(
                    registerRequestDTO.imageProfile(),
                    registerRequestDTO.email()
            );
            verify(userRepository, never()).save(any());
            verify(passwordEncoder, never()).encode(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
            verify(rolesRepository, never()).findByRoleType(any());
        }

        @Test
        void saveUser_roleNotExist_throwsException(){
            doNothing().when(tikaService).validateFile(any());
            when(userRepository.existsByEmail(registerRequestDTO.email()))
                    .thenReturn(false);
            when(cloudinaryService.uploadImageProfile(
                    registerRequestDTO.imageProfile(),
                    registerRequestDTO.email()))
                        .thenReturn("image-url");

            when(rolesRepository.findByRoleType(RoleCategory.ROLE_USER))
                    .thenReturn(Optional.empty());

            assertThrows(RuntimeException.class, () -> {
                authService.saveUser(registerRequestDTO);
            });

            verify(rolesRepository).findByRoleType(RoleCategory.ROLE_USER);
            verify(cloudinaryService).uploadImageProfile(
                    registerRequestDTO.imageProfile(),
                    registerRequestDTO.email()
            );
            verify(userRepository, never()).save(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
            verify(passwordEncoder).encode(registerRequestDTO.password());
        }
    }

    @Nested
    class userLogin{
        @Test
        void userLogin_pass(){
            User user = new User();
            user.setId(userId);
            user.setUsername("nikos");
            user.setEmail("test@gmail.com");
            user.setImageProfile("image-test-123");

            userPrincipal = new UserPrincipal(user);

            authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    Collections.emptyList()
            );

            when(authenticationManager.authenticate(any()))
                    .thenReturn(authentication);

            when(jwtUtils.generateToken(userPrincipal))
                    .thenReturn("testToken-123");


           WrapperLoginResDTO result = authService.userLogin(loginRequestDTO);

           assertEquals(userPrincipal.getId(), result.user().id());
           assertEquals(userPrincipal.getDisplayName(), result.user().username());
           assertEquals(userPrincipal.getEmail(), result.user().email());
           assertEquals(userPrincipal.getImageProfile(), result.user().imageProfile());
           assertEquals("testToken-123", result.token());

           verify(auditPublisher).publishEvent(
                    eq(userId),
                    eq(UserActionsCategory.USER_LOGGED_IN),
                    eq("USER"),
                    any()
            );
           verify(authenticationManager).authenticate(
                   new  UsernamePasswordAuthenticationToken(
                           loginRequestDTO.email(),
                           loginRequestDTO.password()
                   )
           );
        }

        @Test
        void userLogin_AuthenticationFailed_throwsException(){
            when(authenticationManager.authenticate(any()))
                    .thenThrow(new BadCredentialsException("bad credentials"));

            assertThrows(BadCredentialsException.class, ()-> {
                authService.userLogin(loginRequestDTO);
            });

            verify(authenticationManager).authenticate(any());
            verify(jwtUtils, never()).generateToken(any());
        }

        @Test
        void userLogin_generateTokenFailed_throwException(){
            User user = new User();
            user.setId(userId);
            user.setUsername("nikos");
            user.setEmail("test@gmail.com");
            user.setImageProfile("image-test-123");

            userPrincipal = new UserPrincipal(user);

            authentication = new UsernamePasswordAuthenticationToken(
                    userPrincipal,
                    null,
                    Collections.emptyList()
            );

            when(authenticationManager.authenticate(any()))
                    .thenReturn(authentication);

            when(jwtUtils.generateToken(any()))
                    .thenThrow(new RuntimeException("JWT failed"));

            assertThrows(RuntimeException.class, () ->
                    authService.userLogin(loginRequestDTO)
            );

            verify(jwtUtils).generateToken(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
        }
    }

    @Nested
    class changeAccountInfo{

        @Test
        void changeAccountInfo_success(){
            User user = new User();
            user.setId(userId);
            user.setUsername("username");
            user.setEmail("test@gmail.com");
            user.setPassword("pass123");
            user.setImageProfile("image-test-123");

            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    "newUsername",
                    "newEmail@gmail.com"
            );


            when(userRepository.findByEmailBasic(userId))
                    .thenReturn(Optional.of(user));

           UpdateAccountResDTO result = authService.changeAccountInfo(
                   user.getId(),
                   updateAccountReqDTO
           );

           assertEquals("Updated account successfully", result.message());
           assertEquals("newUsername", result.username());
           assertEquals("newEmail@gmail.com", result.email());

           verify(userRepository).findByEmailBasic(userId);
           verify(userRepository).save(user);
            verify(cache).invalidate("test@gmail.com");
        }
        @Test
        void changeAccountInfo_invalidUsernameAndValidEmail_success(){
            User user = new User();
            user.setId(userId);
            user.setUsername("test1");
            user.setPassword("pass123");
            user.setEmail("test@gmail.com");
            user.setImageProfile("image-test-123");

            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    null,
                    "new@gmail.com"
            );

            when(userRepository.findByEmailBasic(user.getId()))
                    .thenReturn(Optional.of(user));

            UpdateAccountResDTO result = authService.changeAccountInfo(
                    user.getId(),
                    updateAccountReqDTO
            );

            assertEquals("test1", result.username());
            assertEquals("new@gmail.com", result.email());
            assertEquals("Updated account successfully", result.message());

            verify(userRepository).findByEmailBasic(user.getId());
            verify(userRepository).save(user);
            verify(cache).invalidate("test@gmail.com");

        }

        @Test
        void changeAccountInfo_validUsernameAndInvalidEmail_success(){
            User user = new User();
            user.setId(userId);
            user.setUsername("username");
            user.setPassword("pass123");
            user.setEmail("test@gmail.com");
            user.setImageProfile("image-test-123");

            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    "newUsername",
                    null
            );

            when(userRepository.findByEmailBasic(user.getId()))
                    .thenReturn(Optional.of(user));

            UpdateAccountResDTO result = authService.changeAccountInfo(
                    user.getId(),
                    updateAccountReqDTO
            );

            assertEquals("newUsername", result.username());
            assertEquals("test@gmail.com", result.email());
            assertEquals("Updated account successfully", result.message());

            verify(userRepository).findByEmailBasic(user.getId());
            verify(userRepository).save(user);
            verify(cache).invalidate("test@gmail.com");

        }

        @Test
        void changeAccountInfo_userNotFound_throwException(){
            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    "newUsername",
                    "new@gmail.com"
            );
            when(userRepository.findByEmailBasic(any()))
                    .thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> {
                authService.changeAccountInfo(userId, updateAccountReqDTO);
            });

            verify(userRepository).findByEmailBasic(userId);
            verify(cache, never()).invalidate("test@gmail.com");

        }

        @Test
        void changeAccountInfo_invalidUserNameAndEmail_throwException(){
            User user = new User();
            user.setId(userId);
            user.setUsername("test1");
            user.setPassword("pass123");
            user.setEmail("test@gmail.com");
            user.setImageProfile("image-test-123");

            UpdateAccountReqDTO updateAccountReqDTO = new UpdateAccountReqDTO(
                    null,
                    null
            );

            when(userRepository.findByEmailBasic(user.getId()))
                    .thenReturn(Optional.of(user));

            assertThrows(InvalidAccountUpdateException.class, () -> {
                authService.changeAccountInfo(user.getId(), updateAccountReqDTO);
            });

            verify(userRepository).findByEmailBasic(user.getId());
            verify(cache, never()).invalidate("test@gmail.com");

        }


    }

    @Nested
    class changePassword{

        @Test
        void changePassword_success(){
            User user = new User();
            user.setId(userId);
            user.setUsername("username");
            user.setEmail("test@gmail.com");
            user.setPassword("pass123");
            user.setImageProfile("image-test-123");

            updatePasswordReqDTO = new UpdatePasswordReqDTO("newPass123");

            when(userRepository.findByEmailBasic(userId))
                    .thenReturn(Optional.of(user));
            when(passwordEncoder.encode(updatePasswordReqDTO.password()))
                    .thenReturn("hashedPass");

           UpdatePasswordResDTO result = authService.changePassword(user.getId(), updatePasswordReqDTO);

            assertEquals("Updated password successfully", result.message());
            assertEquals("success", result.status());
            assertEquals("hashedPass", user.getPassword());

            verify(userRepository).save(user);
            verify(userRepository).findByEmailBasic(user.getId());
            verify(passwordEncoder).encode(updatePasswordReqDTO.password());
            verify(cache).invalidate(user.getEmail());
        }

        @Test
        void changePassword_userNotFound(){
            Long userId =1L;
            updatePasswordReqDTO = new UpdatePasswordReqDTO("newPass123");
            when(userRepository.findByEmailBasic(userId))
                    .thenReturn(Optional.empty());

            assertThrows(UserNotFoundException.class, () -> {
                authService.changePassword(userId, updatePasswordReqDTO);
            });

            verify(userRepository).findByEmailBasic(userId);
            verify(passwordEncoder, never()).encode(updatePasswordReqDTO.password());
            verify(cache, never()).invalidate("test@gmail.com");

        }
    }
}
