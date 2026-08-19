package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.dto.AddIncomeRequestDTO;
import com.example.ExpenseTracker.dto.IncomeDataDTO;
import com.example.ExpenseTracker.dto.IncomeResDTO;
import com.example.ExpenseTracker.dto.MonthIncomeTotalInterface;
import com.example.ExpenseTracker.model.IncomeCategory;
import com.example.ExpenseTracker.service.income.IncomeService;
import com.example.ExpenseTracker.util.UserContextUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/income")
@Validated
@Tag(name = "Income", description = "Income management endpoints")
public class IncomeController {

    private final IncomeService incomeService;

    @Operation(
            summary = "Get incomes.",
            description = "Retrieves incomes for user's report.When no incomes exist it returns empty list."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Incomes successfully retrieved"
    )
    @GetMapping("/get-all-incomes")
    public ResponseEntity<List<IncomeDataDTO>> getAllIncomes(Authentication auth){
       List<IncomeDataDTO> list = incomeService.getIncomes(auth);
       return ResponseEntity.ok().body(list);
    }

    @Operation(
            summary = "Get incomes monthly total ",
            description = "Retrieves the current month's total incomes.If no expenses exist, it returns the current month with a zero total"
    )
    @ApiResponse(
            responseCode = "200",
            description = "Month income total data successfully retrieved"
    )
    @GetMapping("income-month-total")
    public ResponseEntity<MonthIncomeTotalInterface> getTotalMonthIncome(){
        Long userId = UserContextUtils.getAuthenticatedUser().getId();
        MonthIncomeTotalInterface list = incomeService.estimateMonthIncomeTotal(userId);
        return ResponseEntity.ok().body(list);
    }

    @Operation(
            summary = "Get income categories.",
            description = "Retrieves income categories."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Income categories successfully retrieved"
    )
    @GetMapping("/get-income-categories")
    public ResponseEntity<List<IncomeCategory>> getIncomeCategories(){
        List<IncomeCategory> list = incomeService.getCategories();
        return ResponseEntity.ok().body(list);
    }

    @Operation(
            summary = "Get income by id.",
            description = "Retrieves specific income filtered by its id."
    )
    @ApiResponse(
            responseCode = "200",
            description = "Income by id successfully retrieved"
    )
    @ApiResponse(
            responseCode = "404",
            description = "Income was not found."
    )
    @GetMapping("/get-income/{incomeId}")
    public ResponseEntity<IncomeDataDTO> getIncome(
            @Parameter(description = "The id of a specific income to be updated.", example = "1")
            @PathVariable Long incomeId, Authentication auth){
      IncomeDataDTO requestedIncome = incomeService.getIncome(incomeId, auth);
      return ResponseEntity.ok().body(requestedIncome);
    }

    @Operation(
            summary = "Add income.",
            description = "Creates a new income record for the user using an idempotency key to prevent concurrent submissions."
    )
    @ApiResponse(
            responseCode = "201",
            description = "Income record is successfully created."
    )
    @PostMapping("/add-income")
    public ResponseEntity<IncomeResDTO> addIncome(
            @Valid @RequestBody AddIncomeRequestDTO addIncomeReqDTO,
            Authentication auth,
            @Parameter(description = "Unique key to prevent duplicate submissions", example = "unique-key-12343")
            @NotBlank(message = "{idempotency.key.required}")
            @Size(min = 10, max = 50, message = "{idempotency.key.invalid.size}")
            @RequestHeader("Idempotency-key") String idempotencyKey
    ){
       IncomeResDTO result = incomeService.addIncome(addIncomeReqDTO, auth, idempotencyKey);
       return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }
}
