package com.example.ExpenseTracker.service.Report;
import com.example.ExpenseTracker.dto.CheckReportExistsDTO;
import com.example.ExpenseTracker.dto.ReportActionResponse;
import com.example.ExpenseTracker.dto.SendReportWebSocketDTO;
import com.example.ExpenseTracker.exception.ReportNotFoundException;
import com.example.ExpenseTracker.model.Expense;
import com.example.ExpenseTracker.model.Notification;
import com.example.ExpenseTracker.model.Report;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.repository.NotificationRepository;
import com.example.ExpenseTracker.repository.ReportRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.service.report.ReportServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ReportServiceImplTest {

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ReportServiceImpl reportService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SimpMessagingTemplate simpMessagingTemplate;

    @BeforeEach
    void setup(){

    };

    @Nested
    class getReportData{
        @Test
        void getReportData_StatusProcessing_success(){
            Long userId = 1L;

            CheckReportExistsDTO dto = mock(CheckReportExistsDTO.class);
            when(dto.getStatus()).thenReturn("PROCESSING");

            when(reportRepository.getExistingReportData(userId))
                    .thenReturn(Optional.of(dto));

            ReportActionResponse result = reportService.getReportData(userId);

            assertEquals("IN_PROGRESS", result.status());
            assertNull(result.csvFilePath());

            verify(reportRepository).getExistingReportData(userId);
        }

        @Test
        void getReportData_statusCreated_success(){
            Long userId = 1L;

            CheckReportExistsDTO dto = mock(CheckReportExistsDTO.class);
            when(dto.getStatus()).thenReturn("CREATED");

            when(reportRepository.getExistingReportData(userId))
                    .thenReturn(Optional.of(dto));

            ReportActionResponse result = reportService.getReportData(userId);

            assertEquals("IN_PROGRESS", result.status());
            assertNull(result.csvFilePath());

            verify(reportRepository).getExistingReportData(userId);
        }

        @Test
        void getReportData_statusDoneAndFreshData_success(){
            Long userId = 1L;

            CheckReportExistsDTO dto = mock(CheckReportExistsDTO.class);
            when(dto.getIsChanged()).thenReturn(false);
            when(dto.getFilePath()).thenReturn("file-123");
            when(dto.getStatus()).thenReturn("DONE");

            when(reportRepository.getExistingReportData(userId))
                    .thenReturn(Optional.of(dto));

            ReportActionResponse result = reportService.getReportData(userId);

            assertEquals("FRESH", result.status());
            assertEquals("file-123", result.csvFilePath());

            verify(reportRepository).getExistingReportData(userId);
        }

        @Test
        void getReportData_statusDoneAndStaleData_success(){
            Long userId = 1L;

            CheckReportExistsDTO dto = mock(CheckReportExistsDTO.class);
            when(dto.getIsChanged()).thenReturn(true);
            when(dto.getFilePath()).thenReturn("file-123");
            when(dto.getStatus()).thenReturn("DONE");

            when(reportRepository.getExistingReportData(userId))
                    .thenReturn(Optional.of(dto));

            ReportActionResponse result = reportService.getReportData(userId);

            assertEquals("STALE", result.status());
            assertEquals("file-123", result.csvFilePath());

            verify(reportRepository).getExistingReportData(userId);
        }

        @Test
        void getReportData_noExistingData_returnsNull_success(){
            Long userId = 1L;

            when(reportRepository.getExistingReportData(userId))
                    .thenReturn(Optional.empty());

            ReportActionResponse result = reportService.getReportData(userId);

            assertNull(result);
        }
    }

    @Nested
    class updateOrCreate{
        @Test
        void updateOrCreate_ReportExists_success(){
            Long userId = 1L;

            User user = new User();
            user.setId(userId);
            String email = "test@test.com";
            String file_url = "file-123";

            Report report = new Report();
            report.setId(100L);


            when(reportRepository.findByUserId(user.getId()))
                    .thenReturn(Optional.of(report));

            reportService.updateOrCreate(user, file_url, email);

            verify(reportRepository).findByUserId(user.getId());

            ArgumentCaptor<Report> reportCaptor = ArgumentCaptor.forClass(Report.class);
            verify(reportRepository).save(reportCaptor.capture());

            Report capturedReport = reportCaptor.getValue();

            assertEquals("file-123", capturedReport.getCsvFile());
            assertFalse(capturedReport.isChanged());
            assertEquals("DONE", capturedReport.getStatus());
            assertEquals(user, capturedReport.getUser());

            ArgumentCaptor<Notification> notifCaptor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(notifCaptor.capture());

            Notification notification = notifCaptor.getValue();

            assertEquals("file-123", notification.getCsvFile());
            assertEquals("FILE_GENERATED", notification.getType());
            assertEquals(user, notification.getUser());

            verify(simpMessagingTemplate).convertAndSendToUser(
                    eq(email),
                    eq("topic/notifications"),
                    any(SendReportWebSocketDTO.class)
            );
        }

        @Test
        void updateOrCreate_ReportNotExist_shouldThrow(){
            Long userId = 1L;
            User user = new User();
            user.setId(userId);
            String file_url = "file-124";
            String email = "test@test.com";

            when(reportRepository.findByUserId(user.getId()))
                    .thenReturn(Optional.empty());

            assertThrows(ReportNotFoundException.class, () -> {
                reportService.updateOrCreate(user, file_url, email);
            });
        }
    }
}
