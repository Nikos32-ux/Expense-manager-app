package com.example.ExpenseTracker.rateLimitingConfig;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import java.time.Duration;


@Getter
@RequiredArgsConstructor
public class RateLimitingPolicy {
    private final int capacity;
    private final int refillToken;
    private final Duration refillPeriod;
}
