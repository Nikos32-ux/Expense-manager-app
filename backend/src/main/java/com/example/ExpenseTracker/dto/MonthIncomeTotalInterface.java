package com.example.ExpenseTracker.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface MonthIncomeTotalInterface {
    Integer getMonth();
    BigDecimal getIncomeTotal();
    
    record MonthIncomeTotalImpl() implements MonthIncomeTotalInterface{
        @Override
        public Integer getMonth() {
            return LocalDate.now().getMonthValue();
        }

        @Override
        public BigDecimal getIncomeTotal() {
            return BigDecimal.ZERO;
        }
    }
    
    static MonthIncomeTotalInterface empty(){
        return new MonthIncomeTotalImpl();
    }
}
