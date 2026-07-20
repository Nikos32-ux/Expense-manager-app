package com.example.ExpenseTracker.testsupport;

import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public abstract class AbstractPostgresTestContainer {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> testDatabase =
            new PostgreSQLContainer<>("postgres:16-alpine");
}