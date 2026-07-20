package com.example.ExpenseTracker.repository;

import com.example.ExpenseTracker.model.ExpenseCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ExpenseCatRepository extends JpaRepository<ExpenseCategory, Long> {
     Optional<ExpenseCategory> findByCategory(String category);
}
