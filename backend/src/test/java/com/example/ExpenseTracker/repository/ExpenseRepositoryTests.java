package com.example.ExpenseTracker.repository;
import com.example.ExpenseTracker.dto.CategoryTotalDTO;
import com.example.ExpenseTracker.dto.ExpenseReportDTO;
import com.example.ExpenseTracker.dto.ExpenseResDTO;
import com.example.ExpenseTracker.dto.MonthExpensesTotalInterface;
import com.example.ExpenseTracker.model.*;
import com.example.ExpenseTracker.testsupport.AbstractPostgresTestContainer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.assertj.core.api.Assertions.assertThat;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@DataJpaTest
public class ExpenseRepositoryTests extends AbstractPostgresTestContainer {

    @Autowired
    ExpenseRepository expenseRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    RolesRepository rolesRepository;

    @Autowired
    ExpenseCatRepository expenseCatRepository;

    Roles role;
    ExpenseCategory category;
    User savedUser;

    @BeforeEach
    void setUp(){
        role = rolesRepository.findByRoleType(RoleCategory.ROLE_USER).orElseThrow();

        savedUser = new User();
        savedUser.setUsername("Nikos");
        savedUser.setEmail("test_" + System.currentTimeMillis() + "@gmail.com");
        savedUser.setPassword("pass123!");
        savedUser.setImageProfile("img.jpg");
        savedUser.getRoles().add(role);

        userRepository.save(savedUser);

        category = expenseCatRepository.findByCategory("Housing").orElseThrow();

    }

    @Nested
    class findExpenseResponse{

        @Test
        void shouldReturnExpense_WhenExpenseExistsAndBelongsToUser(){

            Expense savedExpense = new Expense();
            savedExpense.setAmount(BigDecimal.valueOf(199));
            savedExpense.setDescription("Loan");
            savedExpense.setDate(LocalDateTime.of(2024,12,9, 0,0,0));
            savedExpense.setPayment("card");
            savedExpense.setUser(savedUser);
            savedExpense.setCategory(category);
            expenseRepository.save(savedExpense);

           Optional<ExpenseResDTO> result =
                   expenseRepository.findExpenseResponse(savedExpense.getId(), savedUser.getId());

           assertThat(result).isPresent();
           assertThat(result.get().id()).isEqualTo(savedExpense.getId());
           assertThat(result.get().amount()).isEqualByComparingTo(savedExpense.getAmount());
           assertThat(result.get().description()).isEqualTo(savedExpense.getDescription());
           assertThat(result.get().date()).isEqualTo(savedExpense.getDate());
           assertThat(result.get().payment()).isEqualTo(savedExpense.getPayment());


           assertThat(result.get().categoryId()).isEqualTo(savedExpense.getCategory().getId());
           assertThat(result.get().categoryName()).isEqualTo(savedExpense.getCategory().getCategory());
        }

        @Test
        void shouldReturnEmpty_WhenExpenseExistsButBelongsToOtherUser(){
            User otherUser = new User();
            otherUser.setUsername("Giannis");
            otherUser.setEmail("giannis@gmail.com");
            otherUser.setPassword("pass234!");
            otherUser.setImageProfile("img.jpg");
            otherUser.getRoles().add(role);
            userRepository.save(otherUser);

            Expense savedExpense = new Expense();
            savedExpense.setAmount(BigDecimal.valueOf(199));
            savedExpense.setDescription("Loan");
            savedExpense.setDate(LocalDateTime.of(2024,12,9, 0,0,0));
            savedExpense.setPayment("card");
            savedExpense.setUser(savedUser);
            savedExpense.setCategory(category);
            expenseRepository.save(savedExpense);

            Optional<ExpenseResDTO> result =
                    expenseRepository.findExpenseResponse(savedExpense.getId(), otherUser.getId());

            assertThat(result).isNotPresent();
        }

        @Test
        void shouldReturnEmpty_WhenExpenseDoesNotExist(){
            Long expenseIdInput = 999L;

            Optional<ExpenseResDTO> result =
                    expenseRepository.findExpenseResponse(expenseIdInput, savedUser.getId());

            assertThat(result).isNotPresent();

        }
    }

    @Nested
    class findAllByUserId{

        private void createExpensesForUser(User savedUser, int count){
            for(int i = 1; i<= count; i++){
                Expense expense = new Expense();
                expense.setUser(savedUser);
                expense.setCategory(category);
                expense.setAmount(BigDecimal.valueOf(199));
                expense.setDate(LocalDateTime.of(2026,10,6,0,0));
                expense.setDescription("Test_" + i);
                expense.setPayment("card");

                expenseRepository.save(expense);
            }
        }

        @Test
        void shouldReturnFiveExpenses_whenExpensesBelongToUser(){
            createExpensesForUser(savedUser, 5);
            Pageable pageable = PageRequest.of(0, 5, Sort.by("date").descending());

            Page<ExpenseResDTO> result =
                   expenseRepository.findAllByUserId(savedUser.getId(), pageable);

            assertThat(result)
                    .isNotEmpty()
                    .hasSize(5);
        }


        @Test
        void shouldReturnEmptyPage_whenExpensesExistButNotBelongToUser(){
            User otherUser = new User();
            otherUser.setUsername("Giannis");
            otherUser.setEmail("giannis@gmail.com");
            otherUser.setPassword("pass234!");
            otherUser.setImageProfile("img.jpg");
            otherUser.getRoles().add(role);
            userRepository.save(otherUser);

            createExpensesForUser(savedUser, 5);

            Pageable pageable = PageRequest.of(0, 5, Sort.by("date").descending());

            Page<ExpenseResDTO> result =
                    expenseRepository.findAllByUserId(otherUser.getId(), pageable);

            assertThat(result).isEmpty();
        }

        @Test
        void shouldReturnEmptyPage_whenUserIdDoesNotExist() {
            Pageable pageable = PageRequest.of(0, 5, Sort.by("date").descending());

            Page<ExpenseResDTO> result =
                    expenseRepository.findAllByUserId(111L, pageable);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class monthlyTotalData{
        private void createExpensesForUser(User savedUser, int count){
            for(int i = 1; i<= count; i++){
                Expense expense = new Expense();
                expense.setUser(savedUser);
                expense.setCategory(category);
                expense.setAmount(BigDecimal.valueOf(200));
                expense.setDate(LocalDateTime.of(
                        LocalDate.now().getYear(),
                        LocalDate.now().getMonthValue(),
                        6,0,0));
                expense.setDescription("Test_" + i);
                expense.setPayment("card");

                expenseRepository.save(expense);
            }
        }

        @Test
        void shouldReturnCurrentMonthlyData_whenCurrentMonthHasData(){
            createExpensesForUser(savedUser, 2);

            Optional<MonthExpensesTotalInterface> result =
                    expenseRepository.monthlyTotalData(savedUser.getId());

            assertThat(result).isPresent();
            assertThat(result.get().getMonth()).isEqualTo(LocalDate.now().getMonthValue());
            assertThat(result.get().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(400));
        }

        @Test
        void shouldReturnEmptyOptional_whenCurrentMonthHasNoExpenses(){
            User otherUser = new User();
            otherUser.setUsername("Giannis");
            otherUser.setEmail("giannis@gmail.com");
            otherUser.setPassword("pass234!");
            otherUser.setImageProfile("img.jpg");
            otherUser.getRoles().add(role);
            userRepository.save(otherUser);

            Optional<MonthExpensesTotalInterface> result =
                    expenseRepository.monthlyTotalData(otherUser.getId());

            assertThat(result).isNotPresent();
        }

        @Test
        void shouldReturnEmptyOptional_whenUserHasOnlyPreviousMonthExpenses(){
            Expense expense = new Expense();
            expense.setUser(savedUser);
            expense.setCategory(category);
            expense.setDescription("Test");
            expense.setPayment("card");
            expense.setAmount(BigDecimal.valueOf(500));
            expense.setDate(
                    LocalDateTime.now().minusMonths(1)
            );
            expenseRepository.save(expense);

            Optional<MonthExpensesTotalInterface> result =
                    expenseRepository.monthlyTotalData(savedUser.getId());

            assertThat(result).isNotPresent();
        }
    }

    @Nested
    class findCategoryTotal{
        private void createExpensesForUser(User savedUser, int count){
            for(int i = 1; i<= count; i++){
                Expense expense = new Expense();
                expense.setUser(savedUser);
                expense.setCategory(category);
                expense.setAmount(BigDecimal.valueOf(200));
                expense.setDate(LocalDateTime.now().minusMonths(1));
                expense.setDescription("Test_" + i);
                expense.setPayment("card");

                expenseRepository.save(expense);
            }
        }

        @Test
        void shouldReturnCategoryTotals_whenExpensesExistWithinDateRange(){
            createExpensesForUser(savedUser, 2);
            LocalDateTime startDate = LocalDateTime.now().minusMonths(1);

            List<CategoryTotalDTO> result =
                   expenseRepository.findCategoryTotal(savedUser.getId(), startDate);

            assertThat(result).isNotEmpty();
            assertThat(result.getFirst().getCategory()).isEqualTo("Housing");
            assertThat(result.getFirst().getTotal()).isEqualTo(400.0);
        }

        @Test
        void shouldReturnEmptyList_whenNoExpensesExistWithinDateRange(){
            for(int i = 1; i<= 2; i++){
                Expense expense = new Expense();
                expense.setUser(savedUser);
                expense.setCategory(category);
                expense.setAmount(BigDecimal.valueOf(100));
                expense.setDate(LocalDateTime.now().minusMonths(2));
                expense.setDescription("Test_" + i);
                expense.setPayment("card");

                expenseRepository.save(expense);
            }

            LocalDateTime startDate = LocalDateTime.now().minusMonths(1);

            List<CategoryTotalDTO> result =
                    expenseRepository.findCategoryTotal(savedUser.getId(), startDate);

            assertThat(result).isEmpty();

        }

        @Test
        void shouldReturnMultipleCategoryTotals_whenMultipleCategoriesExist(){
            createExpensesForUser(savedUser, 1);

            ExpenseCategory secondCategory =
                    expenseCatRepository.findByCategory("Utilities").orElseThrow();


            Expense otherCategoryExpense = new Expense();
            otherCategoryExpense.setUser(savedUser);
            otherCategoryExpense.setCategory(secondCategory);
            otherCategoryExpense.setAmount(BigDecimal.valueOf(200));
            otherCategoryExpense.setDate(LocalDateTime.now().minusMonths(1));
            otherCategoryExpense.setDescription("Test2");
            otherCategoryExpense.setPayment("card");

                expenseRepository.save(otherCategoryExpense);


            LocalDateTime startDate = LocalDateTime.now().minusMonths(1);

            List<CategoryTotalDTO> result =
                    expenseRepository.findCategoryTotal(savedUser.getId(), startDate);

            assertThat(result).isNotEmpty();
            assertThat(result.getFirst().getTotal()).isEqualTo(200.0);
            assertThat(result.getFirst().getCategory()).isEqualTo("Housing");

            assertThat(result.get(1).getTotal()).isEqualTo(200.0);
            assertThat(result.get(1).getCategory()).isEqualTo("Utilities");
        }
    }

    @Nested
    class findReportByUserId{
        private void createExpensesForUser(User savedUser, int count){
            for(int i = 1; i<= count; i++){
                Expense expense = new Expense();
                expense.setUser(savedUser);
                expense.setCategory(category);
                expense.setAmount(BigDecimal.valueOf(600));
                expense.setDate(LocalDateTime.of(2026,10,6,0,0));
                expense.setDescription("Test");
                expense.setPayment("card");

                expenseRepository.save(expense);
            }
        }
        @Test
        void shouldReturnReportDataExpenses_whenExpensesExist(){
            createExpensesForUser(savedUser, 1);

            List<ExpenseReportDTO> result =
                    expenseRepository.findReportByUserId(savedUser.getId());

            assertThat(result)
                    .isNotEmpty()
                    .hasSize(1);
            assertThat(result.getFirst().getAmount()).isEqualByComparingTo(BigDecimal.valueOf(600));
            assertThat(result.getFirst().getDescription()).isEqualTo("Test");
            assertThat(result.getFirst().getCategory()).isEqualTo("Housing");
            assertThat(result.getFirst().getExpenseLevel()).isEqualTo("HIGH");
        }

        @Test
        void shouldReturnEmptyList_whenUserHasNoExpenses(){
            List<ExpenseReportDTO> result =
                    expenseRepository.findReportByUserId(savedUser.getId());
            assertThat(result).isEmpty();

        }
    }
}
