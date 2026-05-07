package com.example.ExpenseTracker.mapper;

import com.example.ExpenseTracker.dto.AddIncomeRequestDTO;
import com.example.ExpenseTracker.dto.IncomeResDTO;
import com.example.ExpenseTracker.model.Income;

final public class IncomeMapper {
    public static Income toEntity(AddIncomeRequestDTO addIncomeRequestDTO){
        Income income = new Income();
        income.setAmount(addIncomeRequestDTO.amount());
        income.setSource(addIncomeRequestDTO.source());
        income.setDate(addIncomeRequestDTO.date());

        return income;
    }

    public static IncomeResDTO toDTO(Income income){
        IncomeResDTO incomeResDTO = new IncomeResDTO(
                income.getId(),
                income.getAmount(),
                income.getSource(),
                income.getDate()
        );
        return incomeResDTO;
    }
}
