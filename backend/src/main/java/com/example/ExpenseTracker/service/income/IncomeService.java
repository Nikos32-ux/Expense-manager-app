package com.example.ExpenseTracker.service.income;
import com.example.ExpenseTracker.dto.AddIncomeRequestDTO;
import com.example.ExpenseTracker.dto.IncomeResDTO;
import com.example.ExpenseTracker.dto.MonthIncomeTotalInterface;
import com.example.ExpenseTracker.model.IncomeCategory;
import org.springframework.security.core.Authentication;

import java.util.List;


public interface IncomeService {
    public IncomeResDTO addIncome(AddIncomeRequestDTO incomeRequestDTO, Authentication auth);
    public List<IncomeResDTO> getIncomes(Authentication auth);

    IncomeResDTO getIncome(Long id ,Authentication auth);

    public List<IncomeCategory> getCategories();

    MonthIncomeTotalInterface estimateMonthIncomeTotal(Long userId);
}
