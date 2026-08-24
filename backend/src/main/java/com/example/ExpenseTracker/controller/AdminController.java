package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.AdminOverviewResponse;
import com.example.ExpenseTracker.dto.AdminUserResponse;
import com.example.ExpenseTracker.dto.AuditLogFilters;
import com.example.ExpenseTracker.dto.ResponseAuditLog;
import com.example.ExpenseTracker.service.admin.AdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
@Tag(name = "Admin Dashboard", description = "Endpoints for managing users, audit logs, and overview metrics")
public class AdminController {

    private final AdminService adminService;


    @GetMapping("/overview")
    @Operation(summary = "Get admin overview metrics", description = "Returns total counts for users and expenses.")
    public ResponseEntity<AdminOverviewResponse> getAdminOverview(){
        AdminOverviewResponse data = adminService.getOverview();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/audit-logs")
    @Operation(summary = "Get user actions logs", description = "Returns users actions logs for users.")
    public ResponseEntity<Page<ResponseAuditLog>> getAuditLogs(
            @PageableDefault(page = 0, size = 5,sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
            ){
        Page<ResponseAuditLog> data = adminService.getAuditLogs(pageable);
        return ResponseEntity.ok(data);
    }

    @GetMapping("/users")
    @Operation(summary = "Get users", description = "Fetches users for the admin panel.")
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            @PageableDefault(page = 0, size = 5,sort = "timestamp", direction = Sort.Direction.DESC) Pageable pageable
    ){
     Page<AdminUserResponse> data = adminService.getUsers(pageable);
     return ResponseEntity.ok(data);
    }

    @Operation(summary = "Get user details by email", description = "Fetches a specific user for the admin panel")
    @ApiResponse(responseCode = "200", description = "User found successfully")
    @ApiResponse(responseCode = "404", description = "User not found")
    @GetMapping("/users/{email}")
    public ResponseEntity<AdminUserResponse> getUserByEmail(
            @Parameter(description = "Email of user to retrieve", example = "test@gmail.com")
            @PathVariable String email){
        AdminUserResponse data = adminService.getUserByEmail(email);
        return ResponseEntity.ok(data);
    }
}

