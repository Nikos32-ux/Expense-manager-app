package com.example.ExpenseTracker.service.audit;

import com.example.ExpenseTracker.events.audit.UserActionsEvent;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.model.UserActionsCategory;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.authentication.AuthenticationEventPublisher;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class AuditPublisher {
   private final ApplicationEventPublisher publisher;

   public void publishEvent(Long id, UserActionsCategory action, String actionOn, LocalDateTime when){
       publisher.publishEvent(new UserActionsEvent(id, action, actionOn, when));
   }
}
