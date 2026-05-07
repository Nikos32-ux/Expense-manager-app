package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.model.UserActionsLog;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserActionsEventsRepository extends JpaRepository<UserActionsLog, Long> {

}
