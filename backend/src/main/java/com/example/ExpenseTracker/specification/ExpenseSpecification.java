package com.example.ExpenseTracker.specification;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import com.example.ExpenseTracker.ExpensesFilters;
import com.example.ExpenseTracker.model.Expense;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;

public class ExpenseSpecification {

    public static Specification<Expense> build(ExpensesFilters filters, Long userId){
        return Specification.allOf(
                hasUserId(userId),
                hasCategory(filters.getCategory()),
                hasDate(filters.getMonth()),
                hasMinMax(filters.getAmountMin(), filters.getAmountMax()),
                hasSearch(filters.getSearch())
        );
    }

    public static Specification<Expense> hasCategory(String category){
      return (root,query,cb) -> {
          if(category == null || category.isBlank()) return cb.conjunction();
         return cb.equal(root.get("category").get("category"), category);
      };
    };


    public static Specification<Expense> hasDate(String month) {
        return (root, query, cb) -> {
            if (month == null || month.isBlank()) return cb.conjunction();
            try {
                int monthInput = Month.valueOf(month.toUpperCase()).getValue();
                LocalDateTime start = LocalDateTime.of(LocalDateTime.now().getYear(), monthInput,1,0,0);
                LocalDateTime end = start.plusMonths(1);
                return cb.between(root.get("date"), start, end);
            } catch (IllegalArgumentException e) {
                return cb.conjunction();
            }
        };
    }

    public static Specification<Expense> hasMinMax(Integer amountMin, Integer amountMax){
        return (root,query,cb) -> {
            if(amountMin == null && amountMax == null) return cb.conjunction();
            if(amountMin == null) return cb.lessThanOrEqualTo(root.get("amount"), amountMax);
            if(amountMax == null) return cb.greaterThanOrEqualTo(root.get("amount"), amountMin);

            return cb.between( root.get("amount"), amountMin, amountMax);
        };
    };

    public static Specification<Expense> hasUserId(Long userId){
        return ((root, query, cb) -> {
            return cb.equal(root.get("user").get("id"), userId);
        } );
    }

   public static Specification<Expense> hasSearch(String inputVal){
        return ((root, query, criteriaBuilder) -> {
            if(inputVal == null || inputVal.isBlank()) return criteriaBuilder.conjunction();
            Predicate descriptionField = criteriaBuilder.like(criteriaBuilder.lower(root.get("description")), "%" + inputVal.toLowerCase() + "%" );
            Predicate categoryField = criteriaBuilder.like(criteriaBuilder.lower(root.get("category").get("category")), "%" + inputVal.toLowerCase() + "%" );
            return criteriaBuilder.or(descriptionField, categoryField)
            ;
        });
   }

}
