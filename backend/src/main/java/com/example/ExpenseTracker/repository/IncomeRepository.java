package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.dto.MonthIncomeTotalInterface;
import com.example.ExpenseTracker.model.Income;
import com.example.ExpenseTracker.model.IncomeCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface IncomeRepository extends JpaRepository<Income, Long> {
    public List<Income> getByUserId(Long userId);
    public Optional<Income> findById(Long id);

    @Query("SELECT DISTINCT i.source FROM Income i")
    public List<IncomeCategory> findAllCategories();
    

    @Query(value = """
            SELECT
                EXTRACT(MONTH FROM i.date) as month,
                SUM(i.amount) as incomeTotal
            FROM Income i
            WHERE i.user.id = :userId
            AND EXTRACT(MONTH FROM i.date) = EXTRACT(MONTH FROM CURRENT_DATE)
            AND EXTRACT(YEAR FROM i.date) = EXTRACT(YEAR FROM CURRENT_DATE)
            GROUP BY EXTRACT(MONTH FROM i.date)
            """)
    Optional<MonthIncomeTotalInterface> getMonthIncomeData(@Param("userId") Long userId);
}
