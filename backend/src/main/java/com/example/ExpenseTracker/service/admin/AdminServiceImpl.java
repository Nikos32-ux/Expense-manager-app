package com.example.ExpenseTracker.service.admin;

import com.example.ExpenseTracker.dto.AdminOverviewResponse;
import com.example.ExpenseTracker.dto.AdminUserResponse;
import com.example.ExpenseTracker.dto.ResponseAuditLog;
import com.example.ExpenseTracker.exception.UserNotFoundException;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.UserActionsEventsRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final UserRepository userRepository;
    private final ExpenseRepository expenseRepository;
    private final UserActionsEventsRepository userActionsEventsRepository;


    @Override
    @Transactional(readOnly = true)
    public AdminOverviewResponse getOverview(){
        long totalUsers = userRepository.count();
        long totalExpenses = expenseRepository.count();
        return new AdminOverviewResponse(totalUsers, totalExpenses);
    }

    @Override
    public Page<ResponseAuditLog> getAuditLogs(Pageable pageable){
        return userActionsEventsRepository.getAuditLogs(pageable);
    }

    @Override
    public Page<AdminUserResponse> getUsers(Pageable pageable){
        return userRepository.getUsers(pageable);
    }

    @Override
    public AdminUserResponse getUserByEmail(String email){
       return userRepository.getUser(email)
               .orElseThrow(() -> new UserNotFoundException("User not found"));
    }
}
