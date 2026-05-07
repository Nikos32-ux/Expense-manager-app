package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.AddIncomeRequestDTO;
import com.example.ExpenseTracker.dto.IncomeResDTO;
import com.example.ExpenseTracker.dto.MonthIncomeTotalInterface;
import com.example.ExpenseTracker.model.IncomeCategory;
import com.example.ExpenseTracker.service.income.IncomeService;
import com.example.ExpenseTracker.util.UserContextUtils;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/income")
public class IncomeController {

    private final IncomeService incomeService;

    @GetMapping("/get-all-incomes")
    public ResponseEntity<List<IncomeResDTO>> getAllIncomes(Authentication auth){
       List<IncomeResDTO> list = incomeService.getIncomes(auth);
       return ResponseEntity.ok().body(list);
    }

    @GetMapping("income-month-total")
    public ResponseEntity<MonthIncomeTotalInterface> getTotalMonthIncome(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        MonthIncomeTotalInterface list = incomeService.estimateMonthIncomeTotal(userId);
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/get-income-categories")
    public ResponseEntity<List<IncomeCategory>> getIncomeCategories(){
        List<IncomeCategory> list = incomeService.getCategories();
        return ResponseEntity.ok().body(list);
    }

    @GetMapping("/get-income/{incomeId}")
    public ResponseEntity<IncomeResDTO> getIncome(@PathVariable Long incomeId, Authentication auth){
      IncomeResDTO requestedIncome = incomeService.getIncome(incomeId, auth);
      return ResponseEntity.ok().body(requestedIncome);
    }

    @PostMapping("/add-income")
    public ResponseEntity<IncomeResDTO> addIncome(@Valid @RequestBody AddIncomeRequestDTO addIncomeReqDTO, Authentication auth){
       IncomeResDTO result = incomeService.addIncome(addIncomeReqDTO, auth);
       return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
