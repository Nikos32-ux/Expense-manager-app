package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.dto.CheckReportExistsDTO;
import com.example.ExpenseTracker.dto.ReportStatusProjection;
import com.example.ExpenseTracker.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    Optional<Report> findByUserId(Long userId);

    @Query("SELECT r.status as status FROM Report r WHERE r.user.id = :userId")
     Optional<ReportStatusProjection> findStatusByUserId(@Param("userId") Long userId);

    @Query(value = """
                SELECT
                  r.isChanged as isChanged,
                  r.csvFile as filePath,
                  r.status as status
                FROM Report r
                where r.user.id = :userId
            """)
    Optional<CheckReportExistsDTO> getExistingReportData(@Param("userId") Long userId);

    @Modifying
    @Query("UPDATE Report r SET r.isChanged = true WHERE r.user.id = :userId")
    void markReportStale(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query("""
            UPDATE Report r
            SET r.status = 'PROCESSING'
            WHERE r.user.id = :userId
            AND r.status = 'DONE'
            """)
    Integer setReportStatus(@Param("userId") Long userId);

    @Modifying
    @Transactional
    @Query(value = """
    INSERT INTO reports (user_id, csv_file, is_changed, ws_sent, status)
    VALUES (:userId, NULL, false, false, 'CREATED')
    ON CONFLICT(user_id) DO NOTHING
""", nativeQuery = true)
    int insertReport(@Param("userId") Long userId);



}
