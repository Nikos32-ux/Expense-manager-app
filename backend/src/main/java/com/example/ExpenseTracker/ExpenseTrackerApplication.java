package com.example.ExpenseTracker;

import com.example.ExpenseTracker.config.rateLimitingConfig.RateLimitingProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
public class ExpenseTrackerApplication {

	public static void main(String[] args)  {
		SpringApplication.run
                (ExpenseTrackerApplication.class, args);
	}
}