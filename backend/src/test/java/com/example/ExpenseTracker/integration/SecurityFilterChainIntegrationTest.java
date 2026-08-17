package com.example.ExpenseTracker.integration;
import com.example.ExpenseTracker.model.RoleCategory;
import com.example.ExpenseTracker.model.Roles;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.repository.RolesRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.testsupport.PostgresTestcontainersConfiguration;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.HashSet;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;


@SpringBootTest(properties = {
        "rate-limiting.rules[0].path=/auth/login",
        "rate-limiting.rules[0].capacity=3",
        "rate-limiting.rules[0].refillTokens=1",
        "rate-limiting.rules[0].refillMinutes=10"
})
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(PostgresTestcontainersConfiguration.class)
public class SecurityFilterChainIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RolesRepository rolesRepository;

    User mockUser;
    Roles role;


    @Test
    void shouldDenyAccessToPrivateRoute_whenTokenDoesNotExist() throws Exception{

        mockMvc.perform(get("/auth/get-expenses"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists())
        ;
    }

    @Test
    void shouldDenyAccessToPrivateRoute_whenTokenIsInvalid() throws Exception{

        mockMvc.perform(get("/expenses/get-expenses")
                        .cookie(new Cookie("jwtToken", "invalid-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void shouldReturnForbidden_whenUserAccessesAdminRoute() throws Exception{
        role = rolesRepository.findByRoleType(RoleCategory.ROLE_USER).orElseThrow();

        mockUser = new User();
        mockUser.setUsername("Test");
        mockUser.setPassword(passwordEncoder.encode("TestDev1!"));
        mockUser.setEmail("test@gmail.com");
        mockUser.setImageProfile("file-123");
        mockUser.getRoles().add(role);

        userRepository.save(mockUser);

        MvcResult result = mockMvc.perform(post("/auth/login")
                       .contentType(MediaType.APPLICATION_JSON)
                       .accept(MediaType.APPLICATION_JSON)
                       .content("""
                               {
                                  "email": "test@gmail.com",
                                  "password": "TestDev1!"
                               }
                               """))
                        .andExpect(status().isOk())
                                .andReturn();

        Cookie jwt = result.getResponse().getCookie("jwtToken");
        mockMvc.perform(get("/admin")
                        .cookie(jwt))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturnTooManyRequests_whenLoginEndpointExceedsRateLimitCapacity() throws Exception{
        for(int i = 0; i < 2; i++){
            mockMvc.perform(post("/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                        "email": "test1@gmail.com",
                                        "password": "TestDev123!"
                                    }
                                    """))
                    .andExpect(status().isUnauthorized());
        }

        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .content("""
                              {
                                 "email": "test1@gmail.com",
                                 "password": "TestDev123!"
                              }
                         """)
                )
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.message").value("Too many requests"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

}
