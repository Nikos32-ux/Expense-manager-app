package com.example.ExpenseTracker.dto;
import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthExpensesTotalInterface {
    Integer getMonth();
    BigDecimal getAmount();

    record MonthExpensesTotalImpl() implements MonthExpensesTotalInterface{
        @Override
        public Integer getMonth() {
            return LocalDate.now().getMonthValue();
        }

        @Override
        public BigDecimal getAmount() {
            return BigDecimal.ZERO;
        }
    }

     static MonthExpensesTotalInterface empty(){
        return new MonthExpensesTotalImpl();
    }
}
