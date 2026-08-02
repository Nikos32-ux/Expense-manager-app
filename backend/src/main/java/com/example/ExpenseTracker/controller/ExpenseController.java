package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.mapper.ExpenseMapper;
import com.example.ExpenseTracker.model.Expense;
import com.example.ExpenseTracker.model.ExpenseCategory;
import com.example.ExpenseTracker.service.Expense.ExpenseService;
import com.example.ExpenseTracker.util.UserContextUtils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequiredArgsConstructor
@RequestMapping("/expenses")
@Validated
public class ExpenseController {

    private final ExpenseService expenseService;

    @GetMapping("get-dashboard-expenses")
    public ResponseEntity<Page<ExpenseResDTO>> dashboardExpenses(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        Pageable fixedPageable = PageRequest.of(0,5,Sort.by("date").descending());
        Page<ExpenseResDTO> topExpenses = expenseService.getDashExpenses(userId, fixedPageable);

        return ResponseEntity.ok().body(topExpenses);
    };


    @GetMapping("get-expenses")
    public ResponseEntity<Page<ExpenseResDTO>> getExpenses(
            @Valid @ModelAttribute ExpensesFilters filters,
            @PageableDefault(size = 5, sort = "date", direction = Sort.Direction.DESC) Pageable pageable
    ){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        if(pageable.getSort().isUnsorted()){
            pageable = PageRequest.of(
                    pageable.getPageNumber(),
                    pageable.getPageSize(),
                    Sort.by("date").descending()
            );
        }
        Page<ExpenseResDTO> expenses = expenseService
                .getExpenses(userId, pageable, filters)
                .map(ExpenseMapper::mapToDTO);

        return ResponseEntity.ok(expenses);
    }

    @GetMapping("get-expense/{id}")
    public ResponseEntity<ExpenseResDTO> getExpense(@PathVariable Long id){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        ExpenseResDTO expense = expenseService.getExpenseById(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(expense);
    }

    @GetMapping("expense-month-total")
    public ResponseEntity<MonthExpensesTotalInterface> getTotalSum(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        MonthExpensesTotalInterface monthTotal = expenseService.totalExpenses(userId);
        return ResponseEntity.status(HttpStatus.OK).body(monthTotal);
    }


    @GetMapping("categories-total")
    public ResponseEntity<List<CategoryTotalDTO>> categoriesTotal(
            @Valid
            @ModelAttribute CategoryFilterDTO filterType
    ){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        List<CategoryTotalDTO> listOfTotal = expenseService.estimateCategoryTotal(filterType, userId);
        return ResponseEntity.ok().body(listOfTotal);
    }


    @GetMapping("get-categories")
    public ResponseEntity<List<ExpenseCategory>> getCategories(){
        List<ExpenseCategory> categoriesList = expenseService.getExpenseCategories();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(categoriesList);
    }

    @PostMapping("add_expense")
    public ResponseEntity<AddExpenseResDTO> addExpense(
            @Valid @RequestBody ExpenseReqDTO expenseReqDTO,
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "{idempotency.key.required}")
            @Size(min = 10, max = 50, message = "{idempotency.key.required}")
            String idempotencyKey){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        AddExpenseResDTO createExpenseStatus = expenseService.addExpense(expenseReqDTO, userId, idempotencyKey);
        return  ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createExpenseStatus);
    }

    @PutMapping("update_expense/{expenseId}")
   public ResponseEntity<ExpenseResDTO> updateExpense(@PathVariable Long expenseId, @RequestBody ExpenseReqDTO expenseReqDTO ){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        ExpenseResDTO updated = expenseService.updateExpense(expenseId, expenseReqDTO, userId);
       return ResponseEntity.ok(updated);
   }


    @PutMapping("delete-expense/{expenseId}")
    public ResponseEntity<String> deleteExpense(@PathVariable Long expenseId){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        expenseService.deleteExpense(expenseId, userId);
        return ResponseEntity.noContent().build();
    }
}
