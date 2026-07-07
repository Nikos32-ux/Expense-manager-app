package com.example.ExpenseTracker.service;
import com.example.ExpenseTracker.config.rateLimitingConfig.RateLimitType;
import com.example.ExpenseTracker.config.rateLimitingConfig.RateLimitingProperties;
import com.example.ExpenseTracker.config.rateLimitingConfig.Rule;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class RateLimitingService {

    private final RateLimitingProperties properties;

    public RateLimitingService(RateLimitingProperties properties) {
        this.properties = properties;
    }

    private static final Rule DEFAULT_RULE = new Rule("DEFAULT",50,20, 1);

    public String extractIpOrEmail(HttpServletRequest req){
        Authentication user = SecurityContextHolder.getContext().getAuthentication();
        if(user != null && user.isAuthenticated() && !(user instanceof AnonymousAuthenticationToken)){
            return user.getName();
        }

        if(req.getHeader("X-Real-IP") != null && !req.getHeader("X-Real-IP").isEmpty()){
            return req.getHeader("X-Real-IP");
        }
        
        return req.getRemoteAddr();
    }

    private final Cache<String, Bucket> buckets = Caffeine.newBuilder()
            .expireAfterWrite(1, TimeUnit.HOURS)
            .maximumSize(1000)
            .build();


    public Rule findRateLimitPolicy(String path) {
       List<Rule> rules = properties.getRules();

       return rules.stream()
                .filter(rule -> rule.getPath().equals(path))
                .findFirst()
                .orElse(DEFAULT_RULE);

    }

    private Bucket createBucket(String key, String path){
        Rule type = findRateLimitPolicy(path);
        log.debug("Creating new rate limit bucket for path: {}", path);
        return Bucket.builder()
                .addLimit(limit ->
                        limit.capacity(type.getCapacity())
                                .refillGreedy(type.getRefillTokens(), Duration.ofMinutes(type.getRefillMinutes())))
                .build();
    }

    public Bucket getOrCreateBucket(String key, String path){
        return buckets.get(key, k -> createBucket(k, path));
    }
}