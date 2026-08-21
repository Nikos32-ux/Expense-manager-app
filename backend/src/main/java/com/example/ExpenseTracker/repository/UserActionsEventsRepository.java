package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.dto.AdminUserResponse;
import com.example.ExpenseTracker.dto.ResponseAuditLog;
import com.example.ExpenseTracker.model.UserActionsLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface UserActionsEventsRepository extends JpaRepository<UserActionsLog, Long>, JpaSpecificationExecutor<UserActionsLog> {

    @Query(value = """
               SELECT
                   ual.action as action,
                   u.email as email,
                   ual.timestamp as timestamp
               FROM user_actions_log ual
               JOIN users u ON u.id = ual.user_id
            """, nativeQuery = true)
    Page<ResponseAuditLog> getAuditLogs(Pageable pageable);
}
