package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.NotificationResponseDTO;
import com.example.ExpenseTracker.model.Notification;
import com.example.ExpenseTracker.service.NotificationService;
import com.example.ExpenseTracker.util.UserContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("notifications")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping("get-notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        List<NotificationResponseDTO> list = notificationService.notificationsList(userId);
        return ResponseEntity.ok().body(list);
    }

    @PutMapping("mark-as-read/{notificationId}")
    public ResponseEntity<Void> markNotification(@PathVariable Long notificationId){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        notificationService.markNotification(userId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
