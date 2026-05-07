package com.example.ExpenseTracker;
import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Data
@Getter
@Setter
public class ExpensesFilters {
    private  String month;
    private  String category;
    private  Integer amountMin;
    private  Integer amountMax;
    private  String search;
}
