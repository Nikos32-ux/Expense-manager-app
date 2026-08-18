package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.mapper.ExpenseMapper;
import com.example.ExpenseTracker.model.Expense;
import com.example.ExpenseTracker.model.ExpenseCategory;
import com.example.ExpenseTracker.service.Expense.ExpenseService;
import com.example.ExpenseTracker.util.UserContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Expenses", description = "Expense management endpoints")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
            summary = "Get dashboard page expenses",
            description = "Retrieves a Page containing the five most recent expenses for display on the client dashboard.Returns an empty Page when the user has no expenses"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Dashboard expenses successfully retrieved"
    )
    @GetMapping("get-dashboard-expenses")
    public ResponseEntity<Page<ExpenseResDTO>> dashboardExpenses(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        Pageable fixedPageable = PageRequest.of(0,5,Sort.by("date").descending());
        Page<ExpenseResDTO> topExpenses =

                expenseService.getDashExpenses(userId, fixedPageable);

        return ResponseEntity.ok().body(topExpenses);
    };

    @Operation(
            summary = "Get expenses for transactions page",
            description = "Retrieves paginated expenses for display on the client transactions page. By default, the five most recent expenses are returned"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Transactions page expenses successfully retrieved"
    )
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

    @Operation(
            summary = "Get expense by expense id",
            description = "Retrieves  expense by id"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Expense successfully retrieved"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Expense not found"
    )
    @GetMapping("get-expense/{id}")
    public ResponseEntity<ExpenseResDTO> getExpense(
            @Parameter(description = "Id of expense to retrieve", example = "1")
            @PathVariable Long id){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        ExpenseResDTO expense = expenseService.getExpenseById(id, userId);
        return ResponseEntity.status(HttpStatus.OK).body(expense);
    }

    @Operation(
            summary = "Get expenses monthly total ",
            description = "Retrieves the current month's total expenses.If no expenses exist, it returns the current month with a zero total"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Month total data successfully retrieved"
    )
    @GetMapping("expense-month-total")
    public ResponseEntity<MonthExpensesTotalInterface> getTotalSum(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        MonthExpensesTotalInterface monthTotal = expenseService.totalExpenses(userId);
        return ResponseEntity.status(HttpStatus.OK).body(monthTotal);
    }

    @Operation(
            summary = "Get expense totals for all categories by filter",
            description = "Retrieves a breakdown of total expenses per category and filtered by a day/week/month filter."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Categories totals successfully retrieved"
    )
    @GetMapping("categories-total")
    public ResponseEntity<List<CategoryTotalDTO>> categoriesTotal(
            @Valid
            @ModelAttribute CategoryFilterDTO filterType
    ){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        List<CategoryTotalDTO> listOfTotal = expenseService.estimateCategoryTotal(filterType, userId);
        return ResponseEntity.ok().body(listOfTotal);
    }


    @Operation(
            summary = "Get expense categories",
            description = "Retrieves the list of expense categories."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Categories list successfully retrieved."
    )
    @GetMapping("get-categories")
    public ResponseEntity<List<ExpenseCategory>> getCategories(){
        List<ExpenseCategory> categoriesList = expenseService.getExpenseCategories();
        return ResponseEntity.ok()
                .cacheControl(CacheControl.maxAge(1, TimeUnit.DAYS).cachePublic())
                .body(categoriesList);
    }

    @Operation(
            summary = "Add expense",
            description = "Creates a new expense for the user using an idempotency key to prevent concurrent submissions."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Expense is successfully created"
    )
    @PostMapping("add_expense")
    public ResponseEntity<AddExpenseResDTO> addExpense(
            @Valid @RequestBody ExpenseReqDTO expenseReqDTO,
            @Parameter(description = "Unique key to prevent duplicate submissions", example = "unique-key-12343")
            @RequestHeader("Idempotency-Key")
            @NotBlank(message = "{idempotency.key.required}")
            @Size(min = 10, max = 50, message = "{idempotency.key.invalid.size}")
            String idempotencyKey){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        AddExpenseResDTO createExpenseStatus = expenseService.addExpense(expenseReqDTO, userId, idempotencyKey);
        return  ResponseEntity
                .status(HttpStatus.CREATED)
                .body(createExpenseStatus);
    }

    @Operation(
            summary = "Update expense",
            description = "Updates an expense by id."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Expense is successfully updated."
    )
    @PutMapping("update_expense/{expenseId}")
    public ResponseEntity<ExpenseResDTO> updateExpense(
            @Parameter(description = "Id of existing expense", example = "1")
            @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseReqDTO expenseReqDTO ){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        ExpenseResDTO updated = expenseService.updateExpense(expenseId, expenseReqDTO, userId);
       return ResponseEntity.ok(updated);
    }



    @Operation(
            summary = "Delete an expense",
            description = "Perform soft delete for an expense by id."
    )
    @ApiResponse(
            responseCode = "204",
            description = "Expense is successfully deleted."
    )
    @PutMapping("delete-expense/{expenseId}")
    public ResponseEntity<String> deleteExpense(
            @Parameter(description = "Id of existing expense", example = "1")
            @PathVariable Long expenseId){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        expenseService.deleteExpense(expenseId, userId);
        return ResponseEntity.noContent().build();
    }
}
