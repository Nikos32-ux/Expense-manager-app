package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.model.IdempotentRecords;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface IdempotencyRepository extends JpaRepository<IdempotentRecords, Long>{

    Optional<IdempotentRecords> findByIdempotencyKey(String key);

    @Modifying
    @Query(value = """
            INSERT INTO idempotent_records(idempotency_key, status, created_at, updated_at)
            VALUES(:key, :status, NOW(), NOW())
            ON CONFLICT(idempotency_key) DO NOTHING
            """, nativeQuery = true)
    int createRecord(@Param("key") String key,@Param("status") String status);

    @Modifying
    @Query(value = """
            UPDATE IdempotentRecords ir
            SET ir.status = :status
            WHERE ir.idempotencyKey = :key
            """)
    void markRecordCompleted(
            @Param("key") String key,
            @Param("status") String status
    );
}
