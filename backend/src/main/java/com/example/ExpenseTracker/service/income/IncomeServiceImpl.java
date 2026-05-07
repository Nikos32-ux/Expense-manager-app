package com.example.ExpenseTracker.service.income;
import com.example.ExpenseTracker.dto.AddIncomeRequestDTO;
import com.example.ExpenseTracker.dto.IncomeResDTO;
import com.example.ExpenseTracker.dto.MonthIncomeTotalInterface;
import com.example.ExpenseTracker.exception.IncomeNotFoundException;
import com.example.ExpenseTracker.mapper.IncomeMapper;
import com.example.ExpenseTracker.model.Income;
import com.example.ExpenseTracker.model.IncomeCategory;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.repository.IncomeRepository;
import com.example.ExpenseTracker.repository.UserRepository;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.util.UserContextUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeServiceImpl implements IncomeService {

    private final IncomeRepository incomeRepository;
    private final UserRepository userRepository;


    @Override
    @Cacheable(value = "month-income-total", key = "#userId")
    public MonthIncomeTotalInterface estimateMonthIncomeTotal(Long userId){
        return incomeRepository.getMonthIncomeData(userId)
                .orElse(MonthIncomeTotalInterface.empty());
    }

    @Override
    @Transactional
    @CacheEvict(value = "month-income-total", allEntries = true)
    public IncomeResDTO addIncome(AddIncomeRequestDTO addIncomeReqDTO, Authentication auth){
       UserPrincipal user = (UserPrincipal) auth.getPrincipal();
       Long userId = user.getId();

       User userRef = userRepository.getReferenceById(userId);
       Income income = IncomeMapper.toEntity(addIncomeReqDTO);
       income.setUser(userRef);

       Income savedIncome = incomeRepository.save(income);

       return IncomeMapper.toDTO(savedIncome);
    }

    @Override
    @Transactional
    public List<IncomeResDTO> getIncomes(Authentication auth) {
        UserPrincipal user = (UserPrincipal) auth.getPrincipal();
        Long userId = user.getId();

       List<Income> fetchedIncomeSources = incomeRepository.getByUserId(userId);
       return fetchedIncomeSources.stream()
               .map(IncomeMapper::toDTO)
               .toList();
    }

    @Override
    @Transactional
    public IncomeResDTO getIncome(Long incomeId ,Authentication auth) {
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
