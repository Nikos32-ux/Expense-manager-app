package com.example.ExpenseTracker.service.income;
import com.example.ExpenseTracker.dto.AddIncomeRequestDTO;
import com.example.ExpenseTracker.dto.IncomeDataDTO;
import com.example.ExpenseTracker.dto.IncomeResDTO;
import com.example.ExpenseTracker.dto.MonthIncomeTotalInterface;
import com.example.ExpenseTracker.exception.IncomeNotFoundException;
import com.example.ExpenseTracker.mapper.IncomeMapper;
import com.example.ExpenseTracker.model.IdempotentRecords;
import com.example.ExpenseTracker.model.Income;
import com.example.ExpenseTracker.model.IncomeCategory;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.repository.IdempotencyRepository;
import com.example.ExpenseTracker.repository.IncomeRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.util.UserContextUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;
    private final IdempotencyRepository idempotencyRepository;

    @Override
    @Cacheable(value = "month-income-total", key = "#userId")
    public MonthIncomeTotalInterface estimateMonthIncomeTotal(Long userId){
        return incomeRepository.getMonthIncomeData(userId)
                .orElse(MonthIncomeTotalInterface.empty());
    }

    @Override
    @Transactional
    @CacheEvict(value = "month-income-total", allEntries = true)
    public IncomeResDTO addIncome(AddIncomeRequestDTO addIncomeReqDTO, Authentication auth, String key){
       UserPrincipal user = (UserPrincipal) auth.getPrincipal();
       Long userId = user.getId();
       Optional<IdempotentRecords> record = idempotencyRepository.findByIdempotencyKey(key);
       if(record.isPresent()){
           log.info("RECORD_EXISTS_RETURN_STATUS userId={}, key={}", userId, key);
           return new IncomeResDTO(record.get().getStatus());
       }

        int incomeRecordAdded = idempotencyRepository.createRecord(key, "IN_PROGRESS");
        if(incomeRecordAdded == 0){
            log.info("DUPLICATE_REQUEST_BLOCKED_BY_CONFLICT userId={}, key={}", userId, key);
            return new IncomeResDTO("IN_PROGRESS");
        }
        log.info("IDEMPOTENCY_WINNER_CONTINUING_BUSINESS_LOGIC userId={}, key={}", userId, key);
        User userRef = userRepository.getReferenceById(userId);
        Income income = IncomeMapper.toEntity(addIncomeReqDTO);
        income.setUser(userRef);

        incomeRepository.save(income);
        idempotencyRepository.markRecordCompleted(key, "COMPLETED");

       return new IncomeResDTO("COMPLETED");
    }

    @Override
    @Transactional
    public List<IncomeDataDTO> getIncomes(Authentication auth) {
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        Long userId = user.getId();

       List<Income> fetchedIncomeSources = incomeRepository.getByUserId(userId);
       return fetchedIncomeSources.stream()
               .map(IncomeMapper::toDTO)
               .toList();
    }

    @Override
    @Transactional
    public IncomeDataDTO getIncome(Long incomeId , Authentication auth) {
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        Long userId = user.getId();

        Income fetchedIncome = incomeRepository.findById(incomeId).orElseThrow(() -> new IncomeNotFoundException("Income not found"));
        return IncomeMapper.toDTO(fetchedIncome);
    }

    @Override
    public List<IncomeCategory> getCategories(){
       return incomeRepository.findAllCategories();
    }
}
