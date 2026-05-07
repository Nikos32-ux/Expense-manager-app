package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.ReportActionResponse;
import com.example.ExpenseTracker.model.Report;
import com.example.ExpenseTracker.repository.ReportRepository;
import com.example.ExpenseTracker.service.report.ReportMessageProducer;
import com.example.ExpenseTracker.service.report.ReportService;
import com.example.ExpenseTracker.util.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDateTime;


@RestController
@RequiredArgsConstructor
@RequestMapping("report")
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final ReportMessageProducer reportMessageProducer;
    private final ReportRepository reportRepository;


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
