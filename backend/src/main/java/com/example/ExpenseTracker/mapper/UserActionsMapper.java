package com.example.ExpenseTracker.mapper;


import com.example.ExpenseTracker.events.audit.UserActionsEvent;
import com.example.ExpenseTracker.model.UserActionsLog;

public final class UserActionsMapper {

    public static UserActionsLog mapToUserActionsLogEntity(UserActionsEvent userActionsEvent){
        UserActionsLog userActionsLog = new UserActionsLog();

        userActionsLog.setUserId(userActionsEvent.userId());
        userActionsLog.setAction(userActionsEvent.typeOfAction());
        userActionsLog.setActionOn(userActionsEvent.actionOn());
        userActionsLog.setTimestamp(userActionsEvent.localDateTime());

        return userActionsLog;
    };
}
