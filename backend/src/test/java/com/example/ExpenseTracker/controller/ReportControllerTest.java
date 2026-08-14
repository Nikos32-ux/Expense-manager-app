package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.config.jwtConfig.JwtEntryPoint;
import com.example.ExpenseTracker.dto.ReportActionResponse;
import com.example.ExpenseTracker.model.ExpenseCategory;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.repository.ReportRepository;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.service.JwtFilterService;
import com.example.ExpenseTracker.service.RateLimitingService;
import com.example.ExpenseTracker.service.report.ReportMessageProducer;
import com.example.ExpenseTracker.service.report.ReportService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.hamcrest.Matchers.nullValue;


@WebMvcTest(ReportController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ReportControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    RateLimitingService rateLimitingService;

    @MockitoBean
    JwtEntryPoint jwtEntryPoint;

    @MockitoBean
    JwtFilterService jwtFilterService;

    @MockitoBean
    ReportService reportService;

    @MockitoBean
    ReportMessageProducer reportMessageProducer;

    @MockitoBean
    ReportRepository reportRepository;

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
    class generateReport{

        @Test
        void shouldReturnFresh_whenReportIsFresh() throws Exception{
            ReportActionResponse freshReportActionResponse = new ReportActionResponse(
                    "FRESH",
                    "file-123",
                    LocalDateTime.of(2026,8,12,12,0,0)
            );

            given(reportService.getReportData(mockUser.getId()))
                    .willReturn(freshReportActionResponse);

            mockMvc.perform(post("/report/generate-report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("FRESH"))
                    .andExpect(jsonPath("$.csvFilePath").value("file-123"))
                    .andExpect(jsonPath("$.sentAt").exists());

            verify(reportService).getReportData(mockUser.getId());
            verify(reportService, never()).createIfNeeded(any(),anyLong(),anyString());
        }

        @Test
        void shouldReturnInProgress_whenReportIsInProgress() throws Exception{
            ReportActionResponse inProgressReportActionResponse = new ReportActionResponse(
                    "IN_PROGRESS",
                    null,
                    LocalDateTime.of(2026,8,12,12,0,0)
            );

            given(reportService.getReportData(mockUser.getId()))
                    .willReturn(inProgressReportActionResponse);

            mockMvc.perform(post("/report/generate-report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("IN_PROGRESS"))
                    .andExpect(jsonPath("$.csvFilePath").value(nullValue()))
                    .andExpect(jsonPath("$.sentAt").exists());

            verify(reportService).getReportData(mockUser.getId());
            verify(reportService, never()).createIfNeeded(any(),anyLong(),anyString());
        }

        @Test
        void shouldReturnTaskStarted_whenReportIsStale() throws Exception{
            ReportActionResponse staleReportActionResponse = new ReportActionResponse(
                    "STALE",
                    "file-123",
                    LocalDateTime.of(2026,8,12,12,0,0)
            );

            given(reportService.getReportData(mockUser.getId()))
                    .willReturn(staleReportActionResponse);

            mockMvc.perform(post("/report/generate-report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("TASK_STARTED"))
                    .andExpect(jsonPath("$.csvFilePath").value(nullValue()))
                    .andExpect(jsonPath("$.sentAt").exists());

            verify(reportService).getReportData(mockUser.getId());
            verify(reportService).createIfNeeded(
                    staleReportActionResponse,
                    mockUser.getId(),
                    mockUser.getEmail()
            );
        }

        @Test
        void shouldReturnTaskStarted_whenNoReportExists() throws Exception {
            given(reportService.getReportData(mockUser.getId()))
                    .willReturn(null);

            mockMvc.perform(post("/report/generate-report"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("TASK_STARTED"))
                    .andExpect(jsonPath("$.csvFilePath").value(nullValue()))
                    .andExpect(jsonPath("$.sentAt").exists());

            verify(reportService).getReportData(mockUser.getId());
            verify(reportService).createIfNeeded(
                    null,
                    mockUser.getId(),
                    mockUser.getEmail()
            );
        }
    }
}
