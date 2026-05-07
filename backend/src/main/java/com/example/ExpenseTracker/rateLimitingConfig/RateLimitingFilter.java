package com.example.ExpenseTracker.rateLimitingConfig;
import com.example.ExpenseTracker.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filter) throws ServletException, IOException {

        String key = rateLimitingService.extractIpOrEmail(request);
        String path = request.getRequestURI();

        Bucket bucket = rateLimitingService.newOrExistingBucket(key, path);

        if (bucket.tryConsume(1)) {
            filter.doFilter(request, response);
        } else {

            response.setStatus(429);
            response.setContentType("application/json");

            ObjectMapper objectMapper = new ObjectMapper();

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status: ", 429);
            responseBody.put("message: ", "Too many requests");
            responseBody.put("timestamp: ", LocalTime.now().toString());

            objectMapper.writeValue( response.getOutputStream(), responseBody);
        }
    }
}
