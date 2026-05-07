package com.example.ExpenseTracker.service;
import com.example.ExpenseTracker.rateLimitingConfig.RateLimitingPolicy;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

@Service
public class RateLimitingService {

   public String extractIpOrEmail(HttpServletRequest req){
       Authentication user = SecurityContextHolder.getContext().getAuthentication();
       if(user != null && user.isAuthenticated() && !(user instanceof AnonymousAuthenticationToken)){
           return user.getName();
       }
       return req.getRemoteAddr();
   }

   Cache<String, Bucket> buckets = Caffeine.newBuilder()
           .expireAfterWrite(1, TimeUnit.HOURS)
           .maximumSize(1000)
           .build();

    public RateLimitingPolicy findType(String path) {
        if (path.contains("/auth/login")) return new RateLimitingPolicy(5, 1,  Duration.ofMinutes(1));
        if (path.contains("/auth/user-verify")) {return new RateLimitingPolicy(5, 1, Duration.ofSeconds(10));}
        if (path.contains("/auth/register")) return new RateLimitingPolicy(4,1, Duration.ofMinutes(10));
        if (path.contains("/report/generate-report")) return new RateLimitingPolicy(1, 1, Duration.ofMinutes(5));
        if (path.contains("/auth/update-account-info")) return new RateLimitingPolicy(10,1, Duration.ofMinutes(1));
        if (path.contains("/auth/update-password")) return new RateLimitingPolicy(3,1, Duration.ofMinutes(15));
        return new RateLimitingPolicy(50, 2,  Duration.ofSeconds(1));
    }

   private Bucket createBucket(String path){
       RateLimitingPolicy type = findType(path);
        return Bucket.builder()
                .addLimit(limit -> limit.capacity(type.getCapacity()).refillGreedy(type.getRefillToken(),type.getRefillPeriod())).build();
   }

   public Bucket newOrExistingBucket(String key, String path){
        return buckets.get(key, k -> createBucket(path));
   }
}
