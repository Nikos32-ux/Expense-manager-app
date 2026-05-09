package com.example.ExpenseTracker.service.Expense;
import com.example.ExpenseTracker.ExpensesFilters;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.exception.CategoryNotFoundException;
import com.example.ExpenseTracker.model.ExpenseCategory;
import com.example.ExpenseTracker.repository.ExpenseCatRepository;
import com.example.ExpenseTracker.repository.ReportRepository;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.exception.ExpenseNotFoundException;
import com.example.ExpenseTracker.exception.ResourceNotFoundException;
import com.example.ExpenseTracker.mapper.ExpenseMapper;
import com.example.ExpenseTracker.model.Expense;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.model.UserActionsCategory;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.service.audit.AuditPublisher;
import com.example.ExpenseTracker.specification.ExpenseSpecification;
import com.example.ExpenseTracker.util.DateUtils;
import com.example.ExpenseTracker.util.UserContextUtils;
import lombok.RequiredArgsConstructor;
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
import static java.time.LocalDateTime.now;

@Service
@RequiredArgsConstructor
public class ExpenseServiceImp implements ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseCatRepository expenseCatRepository;
    private final UserRepository userRepository;
    private final AuditPublisher auditPublisher;
    private final ReportRepository reportRepository;


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
    public Expense getExpenseById(Long expenseId, Long userId) {
        Expense expense = expenseRepository.findById(expenseId)
                .orElseThrow(() -> new ExpenseNotFoundException(expenseId));
        Long expenseOwnerId = expense.getUser().getId();
        checkOwnership(userId, expenseOwnerId);
        return expense;
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "monthly-expense-total", key = "#userId"),
            @CacheEvict(value = "reportData", key = "#userId"),
            @CacheEvict(value = "category-total-amount", key = "#userId")
    })
    public ExpenseResDTO addExpense(ExpenseReqDTO expenseReqDTO, Long userId) {
        ExpenseCategory category = expenseCatRepository.findById(expenseReqDTO.categoryId())
                .orElseThrow(() -> new CategoryNotFoundException(expenseReqDTO.categoryId()));
        User refUser = userRepository.getReferenceById(userId);
        LocalDateTime localDateTime = LocalDateTime.of(expenseReqDTO.date(), expenseReqDTO.time());

        Expense expense = ExpenseMapper.mapToEntity(expenseReqDTO);
        expense.setDate(localDateTime);
        expense.setUser(refUser);
        expense.setCategory(category);

        Expense savedExpense = expenseRepository.save(expense);
        reportRepository.markReportStale(userId);

        auditPublisher.publishEvent(refUser.getId(), UserActionsCategory.USER_CREATED_EXPENSE, "USER", now());

        return ExpenseMapper.mapToDTO(savedExpense);
    }

    @Override
    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "monthly-expense-total", key = "#userId"),
            @CacheEvict(value = "reportData", key = "#userId"),
            @CacheEvict(value = "category-total-amount", allEntries = true)
    })
    public ExpenseResDTO updateExpense(Long expenseId, ExpenseReqDTO expenseReqDTO, Long userId) {
        Expense expense = getExpenseById(expenseId, userId);

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
        Expense expense = getExpenseById(id, userId);

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
