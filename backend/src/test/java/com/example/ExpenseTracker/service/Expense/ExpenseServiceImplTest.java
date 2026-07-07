package com.example.ExpenseTracker.service.Expense;
import com.example.ExpenseTracker.dto.AddExpenseResDTO;
import com.example.ExpenseTracker.dto.ExpenseReqDTO;
import com.example.ExpenseTracker.dto.ExpenseResDTO;
import com.example.ExpenseTracker.dto.MonthExpensesTotalInterface;
import com.example.ExpenseTracker.exception.CategoryNotFoundException;
import com.example.ExpenseTracker.exception.ExpenseNotFoundException;
import com.example.ExpenseTracker.exception.ResourceNotFoundException;
import com.example.ExpenseTracker.model.*;
import com.example.ExpenseTracker.repository.*;

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
    private IdempotencyRepository idempotencyRepository;
    
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


    @BeforeEach
    void setUp(){
        expenseReqDTO = new ExpenseReqDTO(
                BigDecimal.valueOf(100),
                10L,
                "Coffee",
                LocalDate.of(2026,1,15),
                LocalTime.of(10,30),
                "CASH"
        );
    }

    @Nested
    class CheckOwnershipTests {
        Long userId = 1L;
        Long expenseOwnerId = 2L;

        @Test
        void checkOwnership_ThrowsErrorOnMismatch() {
            assertThrows(ResourceNotFoundException.class, () -> {
                expenseServiceImp.checkOwnership(userId, expenseOwnerId);
            });
        }

        @Test
        void checkOwnership_DoesNotThrowWhenIdsMatch(){
           Long loggedInUserId = 1L;
           Long expenseOwnerId = 1L;

            assertDoesNotThrow(() -> {
                expenseServiceImp.checkOwnership(userId, expenseOwnerId);
            });
        }
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
    class GetExpenseEntityTests{
        @Test
        void shouldReturnsExpense_whenUserIsOwner(){
            User user = new User();
            user.setId(1L);
            Expense expense = new Expense();
            expense.setId(2L);
            expense.setUser(user);

            when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

            Expense result = expenseServiceImp.getExpenseEntity(expense.getId(), user.getId());

            assertEquals(expense, result);
        }
        @Test
        void shouldThrowResourceNotFound_WhenUserIsNotOwner(){
            User user = new User();
            user.setId(1L);
            User owner = new User();
            owner.setId(2L);

            Expense expense = new Expense();
            expense.setId(2L);
            expense.setUser(owner);

            when(expenseRepository.findById(expense.getId())).thenReturn(Optional.of(expense));

            assertThrows(ResourceNotFoundException.class, () -> {
                expenseServiceImp.getExpenseEntity(expense.getId(),user.getId());
            });
        }
        @Test
        void shouldThrowExpenseNotFound_WhenExpenseNotExist(){
            Long expenseId = 1L;
            Long userId = 2L;
            when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

            assertThrows(ExpenseNotFoundException.class,() -> {
                expenseServiceImp.getExpenseEntity(expenseId, userId);
            });
        }
    }


    @Nested
    class getExpenseByIdTests{
        Long userId = 1L;
        Long expenseId = 2L;
        ExpenseResDTO expenseResDTO = new ExpenseResDTO(
                expenseId,
                BigDecimal.TEN,
                "Lunch",
                LocalDateTime.now(),
                "Cash",
                10L,
                "Food"
        );

        @Test
        void shouldReturnDTO_WhenExpenseExists(){

            when(expenseRepository.findExpenseResponse(expenseId, userId))
                    .thenReturn(Optional.of(expenseResDTO));

            ExpenseResDTO result = expenseServiceImp.getExpenseById(expenseId, userId);

            assertNotNull(result);
            assertEquals(expenseResDTO.id(), result.id());
            assertEquals(expenseResDTO.description(), result.description());
            verify(expenseRepository, times(1))
                    .findExpenseResponse(expenseId, userId);
        }

        @Test
        void shouldThrowException_WhenExpenseNotFound(){
            when(expenseRepository.findExpenseResponse(expenseId, userId))
                    .thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class, () -> {
                expenseServiceImp.getExpenseById(expenseId, userId);
            });
        }
    }


    @Nested
    class AddExpenseTests{
        Long userId = 1L;
        Long categoryId = 10L;
        @Test
            void addExpense_pass_WhenRequestIsNew(){
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
                String idempotencyKey = "123gfdgfdg";

                ExpenseCategory category = new ExpenseCategory();
                category.setId(categoryId);

                LocalDateTime localDateTime = LocalDateTime.of(
                        expenseReqDTO.date(),
                        expenseReqDTO.time()
                );

                when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                        .thenReturn(Optional.empty());
                when(idempotencyRepository.createRecord(idempotencyKey, "IN_PROGRESS"))
                        .thenReturn(1);
                when(userRepository.getReferenceById(userId)).thenReturn(user);
                when(expenseCatRepository.findById(categoryId))
                        .thenReturn(Optional.of(category));

                when(expenseRepository.save(any(Expense.class)))
                        .thenAnswer(i -> i.getArgument(0));

                doNothing().when(reportRepository).markReportStale(userId);

                AddExpenseResDTO result = expenseServiceImp
                        .addExpense(expenseReqDTO, user.getId(), idempotencyKey);

                assertNotNull(result);
                assertEquals("COMPLETED", result.status());

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
            void addExpense_pass_WhenRequestAlreadyProcessed_Completed(){
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
                    String idempotencyKey = "123gfdgfdg";

                    IdempotentRecords record = new IdempotentRecords();
                    record.setId(100L);
                    record.setIdempotencyKey(idempotencyKey);
                    record.setStatus("COMPLETED");

                    when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                            .thenReturn(Optional.of(record));

                    AddExpenseResDTO result = expenseServiceImp
                            .addExpense(expenseReqDTO, user.getId(), idempotencyKey);

                    assertNotNull(result);
                    assertEquals("COMPLETED", result.status());

                verify(expenseRepository, never()).save(any(Expense.class));
                verify(userRepository, never()).getReferenceById(anyLong());
                verify(auditPublisher, never()).publishEvent(anyLong(), any(), anyString(), any());
            }

            @Test
            void addExpense_ShouldReturnInProgress_WhenConcurrentRequestDetected(){
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
                String idempotencyKey = "123gfdgfdg";

                when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                        .thenReturn(Optional.empty());

                when(idempotencyRepository.createRecord(idempotencyKey, "IN_PROGRESS"))
                        .thenReturn(0);

                AddExpenseResDTO result = expenseServiceImp
                        .addExpense(expenseReqDTO, user.getId(), idempotencyKey);

                assertNotNull(result);
                assertEquals("IN_PROGRESS", result.status());

                verify(expenseRepository, never()).save(any(Expense.class));
                verify(auditPublisher, never()).publishEvent(anyLong(), any(), anyString(), any());
            }



        @Test
        void addExpense_NotValidCategory_throwException(){
            String idempotencyKey = "123gfdgfdg";
            IdempotentRecords record = new IdempotentRecords();
            record.setId(100L);
            record.setIdempotencyKey(idempotencyKey);
            record.setStatus("COMPLETED");

            when(idempotencyRepository.findByIdempotencyKey(idempotencyKey))
                    .thenReturn(Optional.empty());
            when(idempotencyRepository.createRecord(idempotencyKey, "IN_PROGRESS"))
                    .thenReturn(1);
            when(expenseCatRepository.findById(categoryId))
                    .thenReturn(Optional.empty());
            assertThrows(CategoryNotFoundException.class, () -> {
                expenseServiceImp.addExpense(expenseReqDTO, userId, idempotencyKey);
            });
            verify(expenseRepository, never()).save(any());
            verify(reportRepository, never()).markReportStale(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
        }
    }


    @Nested
    class UpdateExpenseTests{
        Long userId = 10L;
        Long expenseId = 20L;

        @Test
        void updateExpense_success(){
            User user = new User();
            user.setId(userId);

            ExpenseCategory oldCategory = new ExpenseCategory();
            oldCategory.setId(40L);

            ExpenseCategory newCategory = new ExpenseCategory();
            newCategory.setId(10L);

            LocalDateTime localDateTime = LocalDateTime.of(
                expenseReqDTO.date(),
                expenseReqDTO.time()
            );

            Expense expense = new Expense();
            expense.setId(expenseId);
            expense.setAmount(BigDecimal.valueOf(199));
            expense.setDescription("Shoes");
            expense.setPayment("CASH");
            expense.setDate(LocalDateTime.of(2025, 5, 10, 8, 0));
            expense.setUser(user);
            expense.setCategory(oldCategory);

            when(expenseRepository.findById(expenseId))
                    .thenReturn(Optional.of(expense));
            when(expenseCatRepository.findById(expenseReqDTO.categoryId()))
                    .thenReturn(Optional.of(newCategory));

            when(expenseRepository.save(any(Expense.class)))
                    .thenAnswer(i -> i.getArgument(0));

            ExpenseResDTO result = expenseServiceImp.updateExpense(expenseId, expenseReqDTO, userId);

            assertEquals(expenseReqDTO.amount(), result.amount());
            assertEquals(expenseReqDTO.description(), result.description());
            assertEquals(expenseReqDTO.payment(), result.payment());
            assertEquals(newCategory.getId(), result.categoryId());

            ArgumentCaptor<Expense> expenseCaptor = ArgumentCaptor.forClass(Expense.class);
            verify(expenseRepository).save(expenseCaptor.capture());

            Expense capturedExpense = expenseCaptor.getValue();

            assertEquals(expenseReqDTO.amount(), capturedExpense.getAmount());
            assertEquals(expenseReqDTO.description(), capturedExpense.getDescription());
            assertEquals(expenseReqDTO.payment(), capturedExpense.getPayment());
            assertEquals(expenseReqDTO.categoryId(), capturedExpense.getCategory().getId());
            assertEquals(LocalDateTime.of(expenseReqDTO.date(), expenseReqDTO.time()), capturedExpense.getDate());


            verify(reportRepository).markReportStale(userId);
            verify(auditPublisher).publishEvent(
                    eq(userId),
                    eq(UserActionsCategory.USER_UPDATED_EXPENSE),
                    eq("USER"),
                    any()
            );
        };

        @Test
        void shouldThrowExpenseNotFound(){
            when(expenseRepository.findById(expenseId))
                    .thenReturn(Optional.empty());

            assertThrows(ExpenseNotFoundException.class, () -> {
                expenseServiceImp.updateExpense(expenseId, expenseReqDTO, userId);
            });

            verify(expenseRepository, never()).save(any());
            verify(reportRepository, never()).markReportStale(anyLong());
            verify(auditPublisher, never()).publishEvent(anyLong(), any(), anyString(), any());
        }

        @Test
        void shouldThrowCategoryNotFound(){
            User user = new User();
            user.setId(userId);


            Expense expense = new Expense();
            expense.setId(expenseId);
            expense.setUser(user);
            when(expenseRepository.findById(expenseId))
                    .thenReturn(Optional.of(expense));
            when(expenseCatRepository.findById(expenseReqDTO.categoryId()))
                    .thenReturn(Optional.empty());

            assertThrows(CategoryNotFoundException.class, () -> {
                expenseServiceImp.updateExpense(expenseId, expenseReqDTO, userId);
            });

            verify(expenseRepository, never()).save(any());
            verify(reportRepository, never()).markReportStale(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
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

            verify(expenseRepository, never()).save(any(Expense.class));
            verify(reportRepository, never()).markReportStale(anyLong());
            verify(auditPublisher, never()).publishEvent(anyLong(), any(), anyString(), any());
        }
    }

    @Nested
    class deleteExpenseTests{
        Long userId = 10L;
        Long expenseId = 20L;

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
        void shouldThrowExpenseNotFound(){
            when(expenseRepository.findById(expenseId)).thenReturn(Optional.empty());

            assertThrows(ExpenseNotFoundException.class, () -> {
                expenseServiceImp.deleteExpense(expenseId, userId);
            });

            verify(expenseRepository, never()).deleteById(any());
            verify(reportRepository, never()).markReportStale(any());
            verify(auditPublisher, never()).publishEvent(any(), any(), any(), any());
        }


        @Test
        void shouldThrowResourceNotFound(){
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

