package com.example.ExpenseTracker.service;
import com.example.ExpenseTracker.dto.NotificationResponseDTO;
import com.example.ExpenseTracker.exception.ResourceNotFoundException;
import com.example.ExpenseTracker.model.Notification;
import com.example.ExpenseTracker.repository.NotificationRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class NotificationServiceImp implements NotificationService{
    private final NotificationRepository notificationRepository;


    @Override
    public List<NotificationResponseDTO> notificationsList(Long userId) {
        return notificationRepository.findNotifications(userId);
    }

    @Override
    @Transactional
    public void markNotification(Long userId, Long notificationId) {
        Optional<Notification> notification = notificationRepository.findById(notificationId);
        if(notification.isEmpty()) throw new ResourceNotFoundException("Not found");
        notification.get().setRead(true);
    }
}
