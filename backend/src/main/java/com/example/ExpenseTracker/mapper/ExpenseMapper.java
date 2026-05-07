package com.example.ExpenseTracker.mapper;

import com.example.ExpenseTracker.dto.ExpenseReqDTO;
import com.example.ExpenseTracker.dto.ExpenseResDTO;
import com.example.ExpenseTracker.model.Expense;

public final class ExpenseMapper {

    private ExpenseMapper() {}

    public static Expense mapToEntity(ExpenseReqDTO expenseReqDTO) {
        Expense expense = new Expense();

        if (expenseReqDTO.amount() != null)
            expense.setAmount(expenseReqDTO.amount());

        if (expenseReqDTO.description() != null)
            expense.setDescription(expenseReqDTO.description());

        if (expenseReqDTO.payment() != null)
            expense.setPayment(expenseReqDTO.payment());

        return expense;
    }

    public static ExpenseResDTO mapToDTO(Expense expense) {
        return new ExpenseResDTO(
                expense.getId(),
                expense.getAmount(),
                expense.getDescription(),
                expense.getDate(),
                expense.getPayment(),
                expense.getCategory().getId(),
                expense.getCategory().getCategory()
        );
    }
}