package com.example.ExpenseTracker.config.rateLimitingConfig;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;


@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class Rule {
    private String path;
    private int capacity;
    private int refillTokens;
    private long refillMinutes;
}
