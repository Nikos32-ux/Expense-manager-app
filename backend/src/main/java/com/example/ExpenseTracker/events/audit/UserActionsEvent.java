package com.example.ExpenseTracker.events.audit;

import com.example.ExpenseTracker.model.UserActionsCategory;

import java.time.LocalDateTime;

public record UserActionsEvent(
   Long userId,
   UserActionsCategory typeOfAction,
   String actionOn,
   LocalDateTime localDateTime
) {}
