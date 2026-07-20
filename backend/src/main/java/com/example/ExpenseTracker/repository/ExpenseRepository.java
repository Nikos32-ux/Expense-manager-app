package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.dto.CategoryTotalDTO;
import com.example.ExpenseTracker.dto.ExpenseReportDTO;
import com.example.ExpenseTracker.dto.ExpenseResDTO;
import com.example.ExpenseTracker.dto.MonthExpensesTotalInterface;
import com.example.ExpenseTracker.model.Expense;
import com.example.ExpenseTracker.model.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long>, JpaSpecificationExecutor<Expense> {

        Page<Expense> findByUserId(Long userId, Pageable pageable);
        @Query("SELECT e FROM ExpenseCategory e")
        List<ExpenseCategory> getAllCategories();


    @Query(value = """
                select new com.example.ExpenseTracker.dto.ExpenseResDTO(
                                  e.id,
                                  e.amount,
                                  e.description,
                                  e.date,
                                  e.payment,
                                  c.id,
                                  c.category
                              )
                              from Expense e
                              join e.category c
                              where e.user.id = :userId
                """)
    Page<ExpenseResDTO> findAllByUserId(@Param("userId") Long userId, Pageable pageable);

    @Query("""
             select new com.example.ExpenseTracker.dto.ExpenseResDTO(
                                  e.id,
                                  e.amount,
                                  e.description,
                                  e.date,
                                  e.payment,
                                  c.id,
                                  c.category
                              )
                              from Expense e
                              join e.category c
                              where e.user.id = :userId
                              and e.id = :expenseId
            """)
    Optional<ExpenseResDTO> findExpenseResponse(@Param("expenseId") Long expenseId ,
                                                @Param("userId") Long userId
                                                );

    @Query("""
    SELECT
        EXTRACT(MONTH from e.date) AS month,
        SUM(e.amount) AS amount
    FROM Expense e
    WHERE e.user.id = :userId
      AND EXTRACT(MONTH FROM e.date) = EXTRACT(MONTH FROM CURRENT_DATE)
      AND EXTRACT(YEAR FROM e.date) = EXTRACT(YEAR FROM CURRENT_DATE)
    GROUP BY EXTRACT(MONTH FROM e.date)
""")
    Optional<MonthExpensesTotalInterface> monthlyTotalData(@Param("userId") Long userId);


    @Query(value = """
               SELECT
                   ec.category as category,
                   SUM(e.amount) as total
               FROM expenses e
               JOIN expense_categories ec ON e.category_id = ec.id
               WHERE e.date BETWEEN :startDate AND NOW()
               AND e.user_id = :userId
               GROUP BY ec.category
            """, nativeQuery = true)
    List<CategoryTotalDTO> findCategoryTotal(
            @Param("userId") Long userId,
            @Param("startDate")LocalDateTime startDate
            );

    @Query(value = """
            select
            	e.amount as amount,
            	e.description as description,
            	e.date as date ,
            	to_char(e."date", 'FMMonth') as month,
            	extract(year from e.date)::int as year,
            	e.payment as payment,
            	ec.category as category,
            	case
            		when e.amount > 500 then 'HIGH'
            		when e.amount >100 and e.amount <= 500 then 'MEDIUM'
            		when e.amount < 100 then 'LOW'
            	end as expense_level
            from expenses e
            join expense_categories ec on e.category_id = ec.id
            where e.user_id = :userId
            """, nativeQuery = true)
    List<ExpenseReportDTO> findReportByUserId(@Param("userId") Long userId);

}
