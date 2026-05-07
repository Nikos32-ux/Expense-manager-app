package com.example.ExpenseTracker.service;
import com.example.ExpenseTracker.dto.NotificationResponseDTO;
import com.example.ExpenseTracker.model.Notification;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificationService {
    List<NotificationResponseDTO> notificationsList(Long userId);
    void markNotification(Long userId, Long notificationId);
}
