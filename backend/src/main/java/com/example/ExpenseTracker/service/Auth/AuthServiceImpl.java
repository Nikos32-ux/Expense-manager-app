package com.example.ExpenseTracker.service.Auth;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.exception.InvalidAccountUpdateException;
import com.example.ExpenseTracker.exception.UserNotFoundException;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.exception.EmailAlreadyExistsException;
import com.example.ExpenseTracker.config.jwtConfig.JwtUtils;
import com.example.ExpenseTracker.mapper.AuthRegisterMapper;
import com.example.ExpenseTracker.model.RoleCategory;
import com.example.ExpenseTracker.model.Roles;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.model.UserActionsCategory;
import com.example.ExpenseTracker.repository.RolesRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.service.CloudinaryService;
import com.example.ExpenseTracker.service.TikaService;
import com.example.ExpenseTracker.service.audit.AuditPublisher;
import com.github.benmanes.caffeine.cache.LoadingCache;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final AuditPublisher auditPublisher;
    private final JwtUtils jwtUtils;
    private final RolesRepository rolesRepository;
    private final CloudinaryService cloudinaryService;
    private final TikaService tikaService;
    private final LoadingCache<String, User> cache;

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);

    @Override
    @Transactional
    public RegisterResDTO saveUser(RegisterRequestDTO registerRequestDTO){
        tikaService.validateFile(registerRequestDTO.imageProfile());

       if(userRepository.existsByEmail(registerRequestDTO.email())){
           throw new EmailAlreadyExistsException(registerRequestDTO.email());
       }

       String file_path =  cloudinaryService.uploadImageProfile(
                       registerRequestDTO.imageProfile(),
                       registerRequestDTO.email()
               );

        User user = AuthRegisterMapper.toEntity(registerRequestDTO);

        String password = passwordEncoder.encode(registerRequestDTO.password());
        Roles role = rolesRepository.findByRoleType(RoleCategory.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Role was not found"));

        user.setPassword(password);
        user.getRoles().add(role);
        user.setImageProfile(file_path);

        User savedUser = userRepository.save(user);

        auditPublisher.publishEvent(
                savedUser.getId(),
                UserActionsCategory.USER_REGISTERED,
                "USER",
                now()
        );

       return new RegisterResDTO(
               "success",
               savedUser.getUsername(),
               savedUser.getEmail());
    };

    @Override
    @Transactional
    public WrapperLoginResDTO userLogin(LoginRequestDTO loginRequestDTO){
        UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(
                loginRequestDTO.email(),
                loginRequestDTO.password()
        );

        Authentication authentication = authenticationManager.authenticate(token);
        UserPrincipal user = (UserPrincipal) authentication.getPrincipal();
        String jwt = jwtUtils.generateToken(user);

        LoginResDTO loginResDTO = new LoginResDTO(
                user.getId(),
                user.getDisplayName(),
                user.getEmail(),
                user.getImageProfile()
        );

        auditPublisher.publishEvent(
                user.getId(),
                UserActionsCategory.USER_LOGGED_IN,
                "USER", now()
        );

        return new WrapperLoginResDTO(loginResDTO, jwt);
    }

    @Override
    @Transactional
    public UpdateAccountResDTO changeAccountInfo(Long userId, UpdateAccountReqDTO updateAccount){
        User user = userRepository.findByEmailBasic(userId).orElseThrow(
                () ->  new UserNotFoundException("user.not.found"));
        String oldEmail = user.getEmail();

        if(!StringUtils.hasText(updateAccount.username()) &&
                (!StringUtils.hasText(updateAccount.email()))){
            throw new InvalidAccountUpdateException("account.update.invalid");
        }

        if(StringUtils.hasText(updateAccount.username())){
            user.setUsername(updateAccount.username());
        }

        if(StringUtils.hasText(updateAccount.email())){
            user.setEmail(updateAccount.email());
        }
        cache.invalidate(oldEmail);
        return new UpdateAccountResDTO(
                user.getUsername(),
                user.getEmail(),
                "Updated account successfully");
    }

    @Override
    @Transactional
    public UpdatePasswordResDTO changePassword(Long userId, UpdatePasswordReqDTO updatePassword){
        User user = userRepository.findByEmailBasic(userId).orElseThrow(
                () ->  new UserNotFoundException("User not found"));
        String convertedPassword = passwordEncoder.encode(updatePassword.password());

        user.setPassword(convertedPassword);
        cache.invalidate(user.getEmail());
        return new UpdatePasswordResDTO(
                "Updated password successfully",
                user.getUsername(),
                user.getEmail()
        );
    }
}
