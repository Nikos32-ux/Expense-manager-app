package com.example.ExpenseTracker;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.metrics.buffering.BufferingApplicationStartup;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
@ConfigurationPropertiesScan
public class ExpenseTrackerApplication {

	public static void main(String[] args)  {
		SpringApplication app = new SpringApplication(ExpenseTrackerApplication.class);
        app.setApplicationStartup(new BufferingApplicationStartup(2048));

        app.run(args);
	}
}