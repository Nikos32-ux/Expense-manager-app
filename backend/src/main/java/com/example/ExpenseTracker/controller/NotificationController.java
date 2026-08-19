package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.NotificationResponseDTO;
import com.example.ExpenseTracker.model.Notification;
import com.example.ExpenseTracker.service.NotificationService;
import com.example.ExpenseTracker.util.UserContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("notifications")
@Tag(name = "Notifications", description = "Notifications management endpoints")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(
            summary = "Get notifications.",
            description = "Retrieves notifications for user's report.When no notifications exist it returns empty list."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Notifications successfully retrieved"
    )
    @GetMapping("get-notifications")
    public ResponseEntity<List<NotificationResponseDTO>> getNotifications(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        List<NotificationResponseDTO> list = notificationService.notificationsList(userId);
        return ResponseEntity.ok().body(list);
    }

    @Operation(
            summary = "Mark notification as read",
            description = "Updates a notification's read property to true."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "204",
                    description = "Notification successfully marked as read."
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Notification was not found in the system."
            )
    })
    @PutMapping("mark-as-read/{notificationId}")
    public ResponseEntity<Void> markNotification(
            @Parameter(description = "Notification id", example = "1")
            @PathVariable
            Long notificationId){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        notificationService.markNotification(userId, notificationId);
        return ResponseEntity.noContent().build();
    }
}
