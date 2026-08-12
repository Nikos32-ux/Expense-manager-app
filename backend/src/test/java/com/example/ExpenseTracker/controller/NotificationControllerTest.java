package com.example.ExpenseTracker.controller;
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

import com.example.ExpenseTracker.config.jwtConfig.JwtEntryPoint;
import com.example.ExpenseTracker.dto.NotificationResponseDTO;
import com.example.ExpenseTracker.exception.ResourceNotFoundException;
import com.example.ExpenseTracker.model.ExpenseCategory;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.service.JwtFilterService;
import com.example.ExpenseTracker.service.NotificationService;
import com.example.ExpenseTracker.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

@WebMvcTest(NotificationController.class)
@AutoConfigureMockMvc(addFilters = false)
public class NotificationControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RateLimitingService rateLimitingService;

    @MockitoBean
    JwtEntryPoint jwtEntryPoint;

    @MockitoBean
    JwtFilterService jwtFilterService;

    @MockitoBean
    NotificationService notificationService;

    User mockUser;

    @BeforeEach
    void setUp(){
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("Test");
        mockUser.setEmail("test@gmail.com");
        mockUser.setRoles(new HashSet<>());

        UserPrincipal userPrincipal = new UserPrincipal(mockUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

   @Nested
    class getNotifications{

       @Test
       void shouldReturnNotificationsListAndStatusOk_whenNotificationsExist() throws Exception{
           NotificationResponseDTO notificationResponseDTODTO = new NotificationResponseDTO(
                   1L,
                   LocalDateTime.of(2026,8,12, 12,0,0),
                   "file-123",
                   "FILE_GENERATED",
                   false
           );
           List<NotificationResponseDTO> list = List.of(notificationResponseDTODTO);

           given(notificationService.notificationsList(mockUser.getId()))
                   .willReturn(list);

           mockMvc.perform(get("/notifications/get-notifications"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$[0].id").value(1))
                   .andExpect(jsonPath("$[0].sentAt").value("2026-08-12T12:00:00"))
                   .andExpect(jsonPath("$[0].csv_file").value("file-123"))
                   .andExpect(jsonPath("$[0].type").value("FILE_GENERATED"))
                   .andExpect(jsonPath("$[0].isRead").value(false));

           verify(notificationService).notificationsList(mockUser.getId());
       }

       @Test
       void shouldReturnEmptyNotificationListAndStatusOk_whenNotificationsDoNotExist() throws Exception{

           List<NotificationResponseDTO> list = List.of();

           given(notificationService.notificationsList(mockUser.getId()))
                   .willReturn(list);

           mockMvc.perform(get("/notifications/get-notifications"))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$").isArray())
                   .andExpect(jsonPath("$").isEmpty());

           verify(notificationService).notificationsList(mockUser.getId());
       }
   }

   @Nested
    class markNotification{

        @Test
        void shouldReturnStatusNoContent_whenMarkSucceeds() throws Exception{
            Long notificationId = 10L;

            mockMvc.perform(put("/notifications/mark-as-read/{notificationId}", notificationId))
                    .andExpect(status().isNoContent());

            verify(notificationService).markNotification(mockUser.getId(), notificationId);
        }

       @Test
       void shouldReturnBadRequest_whenMarkSucceeds() throws Exception{
           Long notificationId = 10L;

           mockMvc.perform(put("/notifications/mark-as-read/{notificationId}", notificationId))
                   .andExpect(status().isNoContent());

           verify(notificationService).markNotification(mockUser.getId(), notificationId);
       }

       @Test
       void shouldReturnResourceNotFoundException_whenNotificationDoesNotExist() throws Exception{
           Long notificationId = 10L;
           doThrow(new ResourceNotFoundException("Not found"))
                   .when(notificationService)
                   .markNotification(mockUser.getId(), notificationId);

           mockMvc.perform(put("/notifications/mark-as-read/{notificationId}", notificationId))
                   .andExpect(status().isNotFound())
                   .andExpect(jsonPath("$.message").value("Not found"));

           verify(notificationService).markNotification(mockUser.getId(), notificationId);
       }
   }

}
