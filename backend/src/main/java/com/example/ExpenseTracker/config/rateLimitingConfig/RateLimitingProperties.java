package com.example.ExpenseTracker.config.rateLimitingConfig;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "rate-limiting")
public class RateLimitingProperties {

    List<Rule> rules;
}
