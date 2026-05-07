package com.example.ExpenseTracker.service.report;
import com.example.ExpenseTracker.dto.ReportActionResponse;
import com.example.ExpenseTracker.dto.TaskMessageDTO;


public interface ReportService {
     void createReport(TaskMessageDTO taskMessage);
     ReportActionResponse getReportData(Long userId);
    void createIfNeeded(ReportActionResponse existingReportData, Long userId, String email);
}
