package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.dto.NotificationResponseDTO;
import com.example.ExpenseTracker.model.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query(value = """
            SELECT new com.example.ExpenseTracker.dto.NotificationResponseDTO(
              n.id,
              n.sentAt,
              n.csvFile,
              n.type,
              n.isRead
            )
            FROM Notification n
            WHERE n.user.id = :userId
            """)
    List<NotificationResponseDTO> findNotifications(@Param("userId") Long userId);


}
