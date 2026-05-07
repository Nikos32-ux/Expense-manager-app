package com.example.ExpenseTracker.util;
import java.time.LocalDateTime;
public final class DateUtils {

    public static LocalDateTime processFilter(String filter){
       return switch (filter) {
            case "day" -> LocalDateTime.now().minusDays(1);
            case "week" -> LocalDateTime.now().minusDays(7);
            case "month" -> LocalDateTime.now().minusDays(30);
            default ->  LocalDateTime.now();
        };
    }


}
