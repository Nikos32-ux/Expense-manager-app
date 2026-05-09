package com.example.ExpenseTracker.config.rateLimitingConfig;
import com.example.ExpenseTracker.service.RateLimitingService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class RateLimitingFilter extends OncePerRequestFilter {

    private final RateLimitingService rateLimitingService;

    @PostConstruct
    public void init() {
        log.info("RateLimitingFilter initialized");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filter) throws ServletException, IOException {


        String path = request.getRequestURI();
        String key = rateLimitingService.extractIpOrEmail(request) + path;
        log.debug("Applying rate limiting for key: {}", key);

        Bucket bucket = rateLimitingService.getOrCreateBucket(key, path);

        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (probe.isConsumed()) {
            log.debug("Request allowed for key: {}, remaining tokens: {}",
                    key,
                    probe.getRemainingTokens());
            filter.doFilter(request, response);
            return;
        }
            long nanos = probe.getNanosToWaitForRefill();
            long seconds = TimeUnit.NANOSECONDS.toSeconds(nanos);
            ObjectMapper objectMapper = new ObjectMapper();

        log.warn("Rate limit exceeded for key: {}, retry after: {} seconds",
                key,
                seconds);
            response.setStatus(429);
            response.setContentType("application/json");
            response.setHeader("Retry-After", String.valueOf(seconds));

            Map<String, Object> responseBody = new HashMap<>();
            responseBody.put("status", 429);
            responseBody.put("message", "Too many requests");
            responseBody.put("timestamp", LocalTime.now().toString());

            objectMapper.writeValue( response.getOutputStream(), responseBody);

    }
}
