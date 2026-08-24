package com.example.ExpenseTracker.service.admin;

import com.example.ExpenseTracker.dto.AdminOverviewResponse;
import com.example.ExpenseTracker.dto.AdminUserResponse;
import com.example.ExpenseTracker.dto.ResponseAuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AdminService {
    AdminOverviewResponse getOverview();
    Page<ResponseAuditLog> getAuditLogs(Pageable pageable);
    Page<AdminUserResponse> getUsers(Pageable pageable);
    AdminUserResponse getUserByEmail(String email);
}
