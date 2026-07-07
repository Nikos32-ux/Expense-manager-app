package com.example.ExpenseTracker.service.Expense;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.exception.CategoryNotFoundException;
import com.example.ExpenseTracker.model.*;
import com.example.ExpenseTracker.repository.*;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.exception.ExpenseNotFoundException;
import com.example.ExpenseTracker.exception.ResourceNotFoundException;
import com.example.ExpenseTracker.mapper.ExpenseMapper;
import com.example.ExpenseTracker.service.audit.AuditPublisher;
import com.example.ExpenseTracker.specification.ExpenseSpecification;
import com.example.ExpenseTracker.util.DateUtils;
import com.example.ExpenseTracker.util.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
@Slf4j
public class ExpenseServiceImp implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCatRepository expenseCatRepository;
    private final UserRepository userRepository;
    private final AuditPublisher auditPublisher;
    private final ReportRepository reportRepository;
    private final IdempotencyRepository idempotencyRepository;


    public void checkOwnership(Long loggedInUserId, Long expenseOwnerId) {
        if (!loggedInUserId.equals(expenseOwnerId)) {
            throw new ResourceNotFoundException("Resource not found");
        }
    }


    @Override
    @Cacheable(value = "monthly-expense-total", key = "#userId")
    public MonthExpensesTotalInterface totalExpenses(Long userId){
        return expenseRepository.monthlyTotalData(userId)
                .orElse(MonthExpensesTotalInterface.empty());
    }


    @Override
    @Cacheable(value = "category-total-amount", key = "#userId + '_' + #categoryFilterDTO")
    public List<CategoryTotalDTO> estimateCategoryTotal( CategoryFilterDTO categoryFilterDTO, Long userId){
        LocalDateTime startDate = DateUtils.processFilter(categoryFilterDTO.filter());
        return expenseRepository.findCategoryTotal(userId, startDate);
    }

    @Override
    public Page<ExpenseResDTO> getDashExpenses(Long userId, Pageable pageable){
        return expenseRepository.findAllByUserId(userId, pageable);
    }


    @Override
    public Page<Expense> getExpenses(Long userId, Pageable pageable, ExpensesFilters filters) {
        Specification<Expense> finalSpec = ExpenseSpecification.build(filters, userId);
        return expenseRepository.findAll(finalSpec, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Expense getExpenseEntity(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
        Long expenseOwnerId = expense.getUser().getId();
        checkOwnership(userId, expenseOwnerId);
        return expense;
    }

    @Transactional(readOnly = true)
    public ExpenseResDTO getExpenseById(Long expenseId, Long userId) {
        return expenseRepository.findExpenseResponse(expenseId, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Resource not found"));
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "monthly-expense-total", key = "#userId"),
            @CacheEvict(value = "reportData", key = "#userId"),
            @CacheEvict(value = "category-total-amount", allEntries = true)
    })
    public AddExpenseResDTO addExpense(ExpenseReqDTO expenseReqDTO, Long userId, String idempotencyKey) {
         log.info("ADD_EXPENSE_REQUEST_RECEIVED userId={}, key={}", userId, idempotencyKey);

        Optional<IdempotentRecords> record = idempotencyRepository.findByIdempotencyKey(idempotencyKey);

        if(record.isPresent()){
            log.info("RECORD_EXISTS_RETURN_STATUS userId={}, key={}", userId, idempotencyKey);
            return new AddExpenseResDTO(record.get().getStatus());
        }

        int recordAdded = idempotencyRepository.createRecord(idempotencyKey, "IN_PROGRESS");

        if(recordAdded == 0){
            log.info("DUPLICATE_REQUEST_BLOCKED_BY_CONFLICT userId={}, key={}", userId, idempotencyKey);
            return new AddExpenseResDTO("IN_PROGRESS");
        }

        log.info("IDEMPOTENCY_WINNER_CONTINUING_BUSINESS_LOGIC userId={}, key={}", userId, idempotencyKey);
        ExpenseCategory category = expenseCatRepository.findById(expenseReqDTO.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(expenseReqDTO.categoryId()));

        User refUser = userRepository.getReferenceById(userId);
        LocalDateTime localDateTime = LocalDateTime.of(expenseReqDTO.date(), expenseReqDTO.time());

        Expense expense = ExpenseMapper.mapToEntity(expenseReqDTO);
        expense.setDate(localDateTime);
        expense.setUser(refUser);
        expense.setCategory(category);

        expenseRepository.save(expense);
        idempotencyRepository.markRecordCompleted(idempotencyKey, "COMPLETED");
        reportRepository.markReportStale(userId);

        auditPublisher.publishEvent(refUser.getId(), UserActionsCategory.USER_CREATED_EXPENSE, "USER", now());

        return new AddExpenseResDTO("COMPLETED");
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "monthly-expense-total", key = "#userId"),
            @CacheEvict(value = "reportData", key = "#userId"),
            @CacheEvict(value = "category-total-amount", allEntries = true)
    })
    public ExpenseResDTO updateExpense(Long expenseId, ExpenseReqDTO expenseReqDTO, Long userId) {
        Expense expense = getExpenseEntity(expenseId, userId);

        LocalDateTime updatedTime = LocalDateTime.of(expenseReqDTO.date(), expenseReqDTO.time());
        ExpenseCategory category = expenseCatRepository.findById(expenseReqDTO.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(expenseReqDTO.categoryId()));

        expense.setAmount(expenseReqDTO.amount());
        expense.setDescription(expenseReqDTO.description());
        expense.setPayment(expenseReqDTO.payment());
        expense.setDate(updatedTime);
        expense.setCategory(category);

        Expense saved = expenseRepository.save(expense);

        reportRepository.markReportStale(userId);
        auditPublisher.publishEvent(expense.getUser().getId(), UserActionsCategory.USER_UPDATED_EXPENSE, "USER", now());

        return ExpenseMapper.mapToDTO(saved);
    }



    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "monthly-expense-total", key = "#userId"),
            @CacheEvict(value = "reportData", key = "#userId"),
            @CacheEvict(value = "category-total-amount", allEntries = true)
    })
    public void deleteExpense(Long id, Long userId) {
        Expense expense = getExpenseEntity(id, userId);

        expenseRepository.deleteById(expense.getId());
        reportRepository.markReportStale(userId);
        auditPublisher.publishEvent(
                expense.getUser().getId(),
                UserActionsCategory.USER_DELETED_EXPENSE,
                "USER", now()
        );
    }


    @Override
    public List<ExpenseCategory> getExpenseCategories(){
       return expenseRepository.getAllCategories();
    }
}
