package com.example.ExpenseTracker.events.audit;


import com.example.ExpenseTracker.mapper.UserActionsMapper;
import com.example.ExpenseTracker.model.UserActionsLog;
import com.example.ExpenseTracker.repository.UserActionsEventsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
@Slf4j
public class UserActionsListener {

   private final UserActionsEventsRepository userActionsEventsRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async
    public void handleAction(UserActionsEvent userActionsEvent){
        try{
            UserActionsLog action = UserActionsMapper.mapToUserActionsLogEntity(userActionsEvent);
            userActionsEventsRepository.save(action);
        }
        catch (Exception e){
            log.error("Failed to persist to audit log table", e);
        }

    }
}
