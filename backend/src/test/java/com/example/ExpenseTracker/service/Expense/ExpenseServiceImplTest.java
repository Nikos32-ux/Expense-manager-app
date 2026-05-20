package com.example.ExpenseTracker.service.Expense;
import com.example.ExpenseTracker.dto.AddExpenseResDTO;
import com.example.ExpenseTracker.dto.ExpenseReqDTO;
import com.example.ExpenseTracker.dto.ExpenseResDTO;
import com.example.ExpenseTracker.dto.MonthExpensesTotalInterface;
import com.example.ExpenseTracker.exception.CategoryNotFoundException;
import com.example.ExpenseTracker.exception.ExpenseNotFoundException;
import com.example.ExpenseTracker.exception.ResourceNotFoundException;
import com.example.ExpenseTracker.model.Expense;
import com.example.ExpenseTracker.model.ExpenseCategory;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.model.UserActionsCategory;
import com.example.ExpenseTracker.repository.ExpenseCatRepository;
import com.example.ExpenseTracker.repository.ExpenseRepository;
import com.example.ExpenseTracker.repository.ReportRepository;
import com.example.ExpenseTracker.repository.UserRepository;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.example.ExpenseTracker.service.audit.AuditPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.Nested;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Optional;


@ExtendWith(MockitoExtension.class)
class ExpenseServiceImplTest {

    @Mock
    private ExpenseRepository expenseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ExpenseCatRepository expenseCatRepository;

    @Mock
    private AuditPublisher auditPublisher;

    @Mock
    private ReportRepository reportRepository;

    @InjectMocks
    private ExpenseServiceImp expenseServiceImp;

    private ExpenseReqDTO expenseReqDTO;
    private final Long userId = 1L;
    private final Long categoryId = 10L;
    private final Long expenseId = 2L;

    @BeforeEach
    void setUp(){
        expenseReqDTO = new ExpenseReqDTO(
                BigDecimal.valueOf(100),
                10L,
                "Coffee",
                LocalDate.now(),
                LocalTime.now(),
                "CASH"
        );
    }
    @Test
    void checkOwnership_ThrowsErrorOnMismatch() {
        Long userId = 1L;
        Long expenseOwnerId = 2L;

        assertThrows(ResourceNotFoundException.class, () -> {
            expenseServiceImp.checkOwnership(userId, expenseOwnerId);
        });
    }

    @Nested
    class totalExpensesTests{
        @Test
        void totalExpenses_success() {
            Long userId = 1L;

            MonthExpensesTotalInterface data = mock(MonthExpensesTotalInterface.class);
            when(data.getMonth()).thenReturn(4);
            when(data.getAmount()).thenReturn(BigDecimal.valueOf(1100));

            when(expenseRepository.monthlyTotalData(userId))
                    .thenReturn(Optional.of(data));

            MonthExpensesTotalInterface result = expenseServiceImp.totalExpenses(userId);

            assertEquals(4,result.getMonth());
            assertEquals(BigDecimal.valueOf(1100),result.getAmount());

            verify(expenseRepository).monthlyTotalData(userId);
        }

        @Test
        void totalExpenses_noMonthData() {
            Long userId = 1L;
            when(expenseRepository.monthlyTotalData(userId))
                    .thenReturn(Optional.empty());

            MonthExpensesTotalInterface result = expenseServiceImp.totalExpenses(userId);

            assertEquals(LocalDate.now().getMonthValue(), result.getMonth());
            assertEquals(BigDecimal.ZERO, result.getAmount());

            verify(expenseRepository).monthlyTotalData(userId);
        }
    }


    @Nested
    class GetExpenseByIdx{
        @Test
        void getExpenseById_whenUserIsOwner_returnsExpense(){
            User user = new User();
            user.setId(1L);
            Expense expense = new Expense();
            expense.setId(2L);
            expense.setUser(user);

            when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));
            Expense result = expenseServiceImp.getExpenseById(expense.getId(), user.getId());
            assertEquals(expense, result);
        }
        @Test
        void getExpenseById_whenUserNotOwner_throwsResourceNotFound(){
            User user = new User();
            user.setId(1L);
            User owner = new User();
            owner.setId(2L);
            Expense expense = new Expense();
            expense.setId(2L);
            expense.setUser(owner);

            when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

            assertThrows(ResourceNotFoundException.class, () -> {
                expenseServiceImp.getExpenseById(expense.getId(),user.getId());
            });
        }
        @Test
        void getExpenseById_whenExpenseNotExist_throwsExpenseNotFound(){
            User user = new User();
            user.setId(1L);
            Expense expense = new Expense();
            expense.setId(2L);
            expense.setUser(user);
            when(expenseRepository.findById(expense.getId())).thenReturn(Optional.empty());

            assertThrows(ExpenseNotFoundException.class,() -> {
                expenseServiceImp.getExpenseById(expense.getId(), user.getId());
            });
        }
    }

    @Nested
    class AddExpense{
        @Test
        void addExpense_pass(){
            expenseReqDTO = new ExpenseReqDTO(
                    BigDecimal.valueOf(100),
                    10L,
                    "Coffee",
                    LocalDate.now(),
                    LocalTime.now(),
                    "CASH"
            );
            User user = new User();
            user.setId(userId);

            ExpenseCategory category = new ExpenseCategory();
            category.setId(categoryId);

            LocalDateTime localDateTime = LocalDateTime.of(
                    expenseReqDTO.date(),
                    expenseReqDTO.time()
            );

            String key = "123gfdgfdg";

            when(userRepository.getReferenceById(userId)).thenReturn(user);
            when(expenseCatRepository.findById(categoryId))
                    .thenReturn(Optional.of(category));

            when(expenseRepository.save(any(Expense.class)))
                    .thenAnswer(i -> i.getArgument(0));

            doNothing().when(reportRepository).markReportStale(userId);

            AddExpenseResDTO result = expenseServiceImp
                    .addExpense(expenseReqDTO, user.getId(), key);

            assertNotNull(result);


            ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
            verify(expenseRepository).save(expenseCaptor.capture());

            assertEquals(100, expenseCaptor.getValue().getAmount().intValue());
            assertEquals("Coffee", expenseCaptor.getValue().getDescription());
            assertEquals("CASH", expenseCaptor.getValue().getPayment().toUpperCase());
            assertEquals(category, expenseCaptor.getValue().getCategory());
            assertEquals(user.getId(), expenseCaptor.getValue().getUser().getId());
            assertEquals(localDateTime, expenseCaptor.getValue().getDate());

            verify(reportRepository).markReportStale(userId);
            verify(expenseCatRepository).findById(expenseReqDTO.categoryId());
            verify(auditPublisher).publishEvent(
                    eq(userId),
                    eq(UserActionsCategory.USER_CREATED_EXPENSE),
                    eq("USER"),
                    any()
            );
        }

        @Test
        void addExpense_NotValidCategory_throwException(){
            String key = "123gfdgfdg";
            when(expenseCatRepository.findById(categoryId))
                    .thenReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> {
                expenseServiceImp.addExpense(expenseReqDTO, userId, key);
            });


            verify(expenseRepository, never()).save(any());
            verify(reportRepository, never()).markReportStale(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
        }
    }

    @Nested
    class UpdateExpense{
        @Test
        void updateExpense_success(){
            Long userId = 10L;
            Long expenseId = 20L;
            expenseReqDTO = new ExpenseReqDTO(
                    BigDecimal.valueOf(100),
                    10L,
                    "Coffee",
                    LocalDate.now(),
                    LocalTime.now(),
                    "CASH"
            );

            User user = new User();
            user.setId(userId);

            ExpenseCategory category = new ExpenseCategory();
            category.setId(categoryId);

            LocalDateTime localDateTime = LocalDateTime.of(
                expenseReqDTO.date(),
                expenseReqDTO.time()
            );

            Expense expense = new Expense();
            expense.setId(expenseId);
            expense.setAmount(BigDecimal.valueOf(100));
            expense.setDescription("Coffee");
            expense.setPayment("CASH");
            expense.setDate(LocalDateTime.now());
            expense.setUser(user);
            expense.setCategory(category);

            when(expenseRepository.findById(expenseId))
                    .thenReturn(Optional.of(expense));
            when(expenseCatRepository.findById(categoryId))
                    .thenReturn(Optional.of(category));

            when(expenseRepository.save(any(Expense.class)))
                    .thenAnswer(i -> i.getArgument(0));


            ExpenseResDTO result = expenseServiceImp.updateExpense(expenseId, expenseReqDTO, userId);

            assertEquals(expenseReqDTO.amount(), result.amount());
            assertEquals(expenseReqDTO.description(), result.description());
            assertEquals(expenseReqDTO.payment(), result.payment());
            assertEquals(categoryId, result.categoryId());

            ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
            verify(expenseRepository).save(expenseCaptor.capture());

            Expense capturedExpense = expenseCaptor.getValue();
            assertEquals(BigDecimal.valueOf(100), capturedExpense.getAmount());
            assertEquals("Coffee", capturedExpense.getDescription());
            assertEquals("CASH", capturedExpense.getPayment());
            assertEquals(localDateTime, capturedExpense.getDate());
            assertEquals(10L, capturedExpense.getCategory().getId());


            verify(reportRepository).markReportStale(userId);
            verify(auditPublisher).publishEvent(
                    eq(userId),
                    eq(UserActionsCategory.USER_UPDATED_EXPENSE),
                    eq("USER"),
                    any()
            );
        };

        @Test
        void updateExpense_expenseNotFound_shouldThrow(){
            User user = new User();
            user.setId(userId);

            when(expenseRepository.findById(expenseId))
                    .thenReturn(Optional.empty());

            assertThrows(ExpenseNotFoundException.class, () -> {
                expenseServiceImp.updateExpense(expenseId, expenseReqDTO, userId);
            });
        }

        @Test
        void updateExpense_categoryNotFound_shouldThrow(){
            User user = new User();
            user.setId(userId);

            Expense expense = new Expense();
            expense.setId(expenseId);
            expense.setUser(user);
            when(expenseRepository.findById(expenseId)).thenReturn(Optional.of(expense));
            when(expenseCatRepository.findById(categoryId)).thenReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> {
                expenseServiceImp.updateExpense(expenseId, expenseReqDTO, userId);
            });
        }

        @Test
        void updateExpense_notExpenseOwner_shouldThrow() {
            Long otherUserId = 100L;

            User owner = new User();
            owner.setId(otherUserId);

            Expense expense = new Expense();
            expense.setId(expenseId);
            expense.setUser(owner);

            when(expenseRepository.findById(expenseId))
                    .thenReturn(Optional.of(expense));

            assertThrows(ResourceNotFoundException.class, () -> {
                expenseServiceImp.updateExpense(expenseId, expenseReqDTO, userId);
            });
        }
    }

    @Nested
    class deleteExpense{

        @Test
        void deleteExpense_pass(){
            User owner = new User();
            owner.setId(userId);

            Expense expense = new Expense();
            expense.setId(expenseId);
            expense.setUser(owner);

          when(expenseRepository.findById(expenseId))
                  .thenReturn(Optional.of(expense));

          expenseServiceImp.deleteExpense(expenseId, userId);

          verify(expenseRepository).deleteById(expenseId);
          verify(reportRepository).markReportStale(userId);
          verify(auditPublisher).publishEvent(
                  eq(userId),
                  eq(UserActionsCategory.USER_DELETED_EXPENSE),
                  eq("USER"),
                  any()
            );
        }

        @Test
        void deleteExpense_ExpenseNotFound_shouldThrow(){
            when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

            assertThrows(ExpenseNotFoundException.class, () -> {
                expenseServiceImp.deleteExpense(expenseId, userId);
            });

            verify(expenseRepository, never()).deleteById(any());
            verify(reportRepository, never()).markReportStale(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
        }


        @Test
        void deleteExpense_NotExpenseOwner_shouldThrow(){
            User someOther = new User();
            someOther.setId(5L);

            Expense expense = new Expense();
            expense.setId(expenseId);
            expense.setUser(someOther);

            when(expenseRepository.findById(expenseId))
                    .thenReturn(Optional.of(expense));

            assertThrows(ResourceNotFoundException.class, () -> {
                expenseServiceImp.deleteExpense(expenseId, userId);
            });

            verify(expenseRepository, never()).deleteById(any());
            verify(reportRepository, never()).markReportStale(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
        }
    }
}

