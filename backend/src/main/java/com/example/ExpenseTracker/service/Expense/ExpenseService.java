package com.example.ExpenseTracker.service.Expense;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.model.Expense;
import com.example.ExpenseTracker.model.ExpenseCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;


import java.util.List;


public interface ExpenseService {
    Page<Expense> getExpenses(Long userId, Pageable pageable, ExpensesFilters filters);
    AddExpenseResDTO addExpense(ExpenseReqDTO expenseReqDTO, Long userId, String key);
    ExpenseResDTO updateExpense(Long id, ExpenseReqDTO expenseReqDTO, Long userId);
    void deleteExpense(Long id, Long userId);
    Expense getExpenseById(Long expenseId, Long UserId);
    List<ExpenseCategory> getExpenseCategories();
    Page<ExpenseResDTO> getDashExpenses( Long userId, Pageable pageable);
    MonthExpensesTotalInterface totalExpenses(Long userId);
    List<CategoryTotalDTO> estimateCategoryTotal(CategoryFilterDTO filterType, Long userId);

}
