package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.ReportActionResponse;
import com.example.ExpenseTracker.model.Report;
import com.example.ExpenseTracker.repository.ReportRepository;
import com.example.ExpenseTracker.service.report.ReportMessageProducer;
import com.example.ExpenseTracker.service.report.ReportService;
import com.example.ExpenseTracker.util.UserContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;


@RestController
@RequiredArgsConstructor
@RequestMapping("report")
@Slf4j
@Tag(name = "Report", description = "Report management endpoint")

public class ReportController {

    private final ReportService reportService;
    private final ReportMessageProducer reportMessageProducer;
    private final ReportRepository reportRepository;

    @Operation(
            summary = "Generate expense report",
            description = "Triggers report generation for the authenticated user. " +
                    "If a report is already fresh, it returns the existing report data immediately. " +
                    "If a report is currently in progress, it notifies the caller. " +
                    "Otherwise, it initiates an asynchronous background task to generate a new report."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Returns the current report status. Can be FRESH (with data), IN_PROGRESS, or TASK_STARTED."
    )
    @PostMapping("generate-report")
    public ResponseEntity<ReportActionResponse> generateReport(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        String email = UserContextUtils.getAuthenticatedUser().getEmail();

        ReportActionResponse existingReportData = reportService.getReportData(userId);

        if(existingReportData != null && existingReportData.status().equals("FRESH")) return ResponseEntity.ok(existingReportData);
        if(existingReportData != null && existingReportData.status().equals("IN_PROGRESS")) return ResponseEntity.ok().body(existingReportData);

        reportService.createIfNeeded(existingReportData, userId, email);

        return ResponseEntity
                .ok()
                .body(new ReportActionResponse( "TASK_STARTED", null, LocalDateTime.now()));
    }
}
