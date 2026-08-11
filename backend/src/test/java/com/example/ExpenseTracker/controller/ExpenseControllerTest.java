package com.example.ExpenseTracker.controller;
import com.example.ExpenseTracker.config.jwtConfig.JwtEntryPoint;
import com.example.ExpenseTracker.dto.*;
import com.example.ExpenseTracker.exception.CategoryNotFoundException;
import com.example.ExpenseTracker.exception.ExpenseNotFoundException;
import com.example.ExpenseTracker.exception.ResourceNotFoundException;
import com.example.ExpenseTracker.model.Expense;
import com.example.ExpenseTracker.model.ExpenseCategory;
import com.example.ExpenseTracker.model.User;
import com.example.ExpenseTracker.security.UserPrincipal;
import com.example.ExpenseTracker.service.Expense.ExpenseService;
import com.example.ExpenseTracker.service.JwtFilterService;
import com.example.ExpenseTracker.service.RateLimitingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.*;
import org.springframework.data.projection.SpelAwareProxyProjectionFactory;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

@WebMvcTest(ExpenseController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ExpenseControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockitoBean
    ExpenseService expenseService;

    @MockitoBean
    RateLimitingService rateLimitingService;

    @MockitoBean
    JwtEntryPoint jwtEntryPoint;

    @MockitoBean
    JwtFilterService jwtFilterService;

    User mockUser;
    Expense expense;
    ExpenseResDTO expenseResDTO;

    @BeforeEach
    void setUpUser(){
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("Test");
        mockUser.setEmail("test@gmail.com");
        mockUser.setRoles(new HashSet<>());

        ExpenseCategory category = new ExpenseCategory();
        category.setId(1L);
        category.setCategory("Food");

        UserPrincipal userPrincipal = new UserPrincipal(mockUser);
        Authentication authentication = new UsernamePasswordAuthenticationToken(userPrincipal, null, userPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

         expenseResDTO = new ExpenseResDTO(
                1L,
                BigDecimal.TEN,
                "Lunch at break",
                LocalDateTime.now(),
                "Cash",
                10L,
                "Food"
        );

         expense = new Expense();

         expense.setId(1L);
         expense.setUser(mockUser);
         expense.setAmount(BigDecimal.valueOf(10));
         expense.setDate(LocalDateTime.now());
         expense.setDescription("Lunch at break");
         expense.setPayment("Cash");
         expense.setCategory(category);

    }

    @Nested
    class getDashboardExpenses{

        @Test
        void shouldReturnDashExpensesPage_whenServiceReturnsExpenses() throws Exception{
            Pageable pageable = PageRequest.of(0,5, Sort.by("date").descending());
            Page<ExpenseResDTO> topExpenses = new PageImpl<>(List.of(expenseResDTO), pageable ,1);

            given(expenseService.getDashExpenses(1L, pageable))
                    .willReturn(topExpenses);

            mockMvc.perform(get("/expenses/get-dashboard-expenses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.totalElements").value(1))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].description").value("Lunch at break"))
                    .andExpect(jsonPath("$.content[0].payment").value("Cash"))
                    .andExpect(jsonPath("$.content[0].categoryName").value("Food"));

            verify(expenseService).getDashExpenses(1L, pageable);
        }

        @Test
        void shouldReturnEmptyPage_whenNoExpensesExist() throws Exception{
            Pageable pageable = PageRequest.of(0,5, Sort.by("date").descending());
            Page<ExpenseResDTO> topExpenses = new PageImpl<>(List.of(), pageable ,0);

            given(expenseService.getDashExpenses(1L, pageable))
                    .willReturn(topExpenses);

            mockMvc.perform(get("/expenses/get-dashboard-expenses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));

            verify(expenseService).getDashExpenses(1L, pageable);

        }
    }

    @Nested
    class getExpenses{

        @Test
        void shouldReturnFirstPageWithDefaultPagination_whenPageParamsAndFiltersAreNotProvided() throws Exception{
            Pageable defaultPageable = PageRequest.of(0,5, Sort.by("date").descending());
            Page<Expense> serviceReturnedExpenses =
                    new PageImpl<>(List.of(expense), defaultPageable ,1);
            ExpensesFilters filters = new ExpensesFilters(null,null,null,null,null);
            given(expenseService.getExpenses(mockUser.getId(), defaultPageable, filters))
                    .willReturn(serviceReturnedExpenses);

            mockMvc.perform(get("/expenses/get-expenses"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].amount").value(10))
                    .andExpect(jsonPath("$.content[0].description").value("Lunch at break"))
                    .andExpect(jsonPath("$.content[0].payment").value("Cash"))
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content.length()").value(1))
                    .andExpect(jsonPath("$.size").value(5));

            verify(expenseService).getExpenses(mockUser.getId(), defaultPageable, filters);
        }

        @Test
        void shouldReturnFirstPageWithCustomPagination_whenFiltersAreNotProvidedAndPageParamsAreProvided() throws Exception{
            Pageable customPageable = PageRequest.of(2,5, Sort.by("date").descending());

            Page<Expense> serviceReturnedExpenses =
                    new PageImpl<>(List.of(expense), customPageable ,1);
            ExpensesFilters filters = new ExpensesFilters(null,null,null,null,null);
            given(expenseService.getExpenses(mockUser.getId(), customPageable, filters))
                    .willReturn(serviceReturnedExpenses);

            mockMvc.perform(get("/expenses/get-expenses")
                    .param("page","2")
                    .param("size", "5"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.pageable.pageNumber").value(2))
                    .andExpect(jsonPath("$.pageable.pageSize").value(5));


            verify(expenseService).getExpenses(mockUser.getId(), customPageable, filters);
        }

        @Test
        void shouldReturnFilteredExpenses_whenFilterParamsAreProvided() throws Exception{
            Pageable customPageable = PageRequest.of(2,5, Sort.by("date").descending());
            Page<Expense> filteredExpenses =
                    new PageImpl<>(List.of(expense), customPageable ,1);
            ExpensesFilters customFilters = new ExpensesFilters("JANUARY","Food",10,100,null);
            given(expenseService.getExpenses(mockUser.getId(), customPageable, customFilters))
                     .willReturn(filteredExpenses);


            mockMvc.perform(get("/expenses/get-expenses")
                    .param("page","2")
                    .param("size","5")
                    .param("month","JANUARY")
                    .param("category", "Food")
                    .param("amountMin","10")
                    .param("amountMax","100"))
                    .andExpect(jsonPath("$.content[0].id").value(1))
                    .andExpect(jsonPath("$.content[0].description").value("Lunch at break"))
                    .andExpect(jsonPath("$.content[0].payment").value("Cash"))
                    .andExpect(jsonPath("$.content[0].categoryId").value(1))
                    .andExpect(jsonPath("$.content[0].categoryName").value("Food"));

            verify(expenseService).getExpenses(mockUser.getId(), customPageable, customFilters);

        }


        @Test
        void shouldReturnBadRequest_whenFilterParamsAreInvalid() throws Exception{
            mockMvc.perform(get("/expenses/get-expenses")
                    .param("month","invalid-value")
                    .param("amountMin", "-10"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.localDate").exists())
                    .andExpect(jsonPath("$.message.month").value("Month must be a valid month name"))
                    .andExpect(jsonPath("$.message.amountMin").value("Minimum amount must be zero or positive"));
        }
    }

    @Nested
    class getExpenseById{

        @Test
        void shouldReturnExpenseById() throws Exception{
            given(expenseService.getExpenseById(expense.getId(), mockUser.getId()))
                    .willReturn(expenseResDTO);

            mockMvc.perform(get("/expenses/get-expense/{id}", expense.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.amount").value(10))
                    .andExpect(jsonPath("$.description").value("Lunch at break"))
                    .andExpect(jsonPath("$.payment").value("Cash"))
                    .andExpect(jsonPath("$.categoryName").value("Food"));;

            verify(expenseService).getExpenseById(expense.getId(), mockUser.getId());
        }

        @Test
        void shouldReturnErrorResponse_whenExpenseDoesNotExist() throws Exception{
            given(expenseService.getExpenseById(10L, mockUser.getId()))
                    .willThrow(new ResourceNotFoundException("Resource not found"));

            mockMvc.perform(get("/expenses/get-expense/{id}", 10L))
                    .andExpect(jsonPath("$.message").value("Resource not found"))
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(status().isNotFound());

            verify(expenseService).getExpenseById(10L, mockUser.getId());
        }

        @Test
        void shouldReturnBadRequest_whenPathVariableInvalid() throws Exception{

            mockMvc.perform(get("/expenses/get-expense/{id}", "abc"))
                    .andExpect(status().isBadRequest());

            verify(expenseService, never()).getExpenseById(any(), anyLong());
        }
    }

    @Nested
    class getTotalSum{

        @Test
        void shouldReturnMonthTotal_whenDataExists() throws Exception{
            SpelAwareProxyProjectionFactory factory = new SpelAwareProxyProjectionFactory();

            Map<String, Object> source = Map.of(
                "month", LocalDateTime.now().getMonthValue(),
                "amount", BigDecimal.valueOf(1999)
            );

            MonthExpensesTotalInterface mockProjection =
                    factory.createProjection(MonthExpensesTotalInterface.class, source);


            given(expenseService.totalExpenses(mockUser.getId()))
                    .willReturn(mockProjection);

            mockMvc.perform(get("/expenses/expense-month-total"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.month").value(LocalDateTime.now().getMonthValue()))
                    .andExpect(jsonPath("$.amount").value(1999));

            verify(expenseService).totalExpenses(mockUser.getId());
        }

        @Test
        void shouldReturnDefaultMonthTotal_whenNoDataFound() throws Exception{
            SpelAwareProxyProjectionFactory factory = new SpelAwareProxyProjectionFactory();
            Map<String, Object> source = Map.of(
                    "month",LocalDateTime.now().getMonthValue(),
                    "amount", BigDecimal.valueOf(50)
            );
            MonthExpensesTotalInterface projection = factory.createProjection(MonthExpensesTotalInterface.class, source);


            given(expenseService.totalExpenses(mockUser.getId()))
                    .willReturn(projection);

            mockMvc.perform(get("/expenses/expense-month-total"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.month").value(LocalDateTime.now().getMonthValue()))
                    .andExpect(jsonPath("$.amount").value(50));

            verify(expenseService).totalExpenses(mockUser.getId());
        }
    }

    @Nested
    class categoriesTotal{

        @Test
        void shouldReturnListOfCategoryTotalAmounts_whenDataExists() throws Exception{
            SpelAwareProxyProjectionFactory factory = new SpelAwareProxyProjectionFactory();

            Map<String, Object> source = Map.of(
                    "category","Food",
                    "total",1899.00
            );
            CategoryTotalDTO categoryTotalDTO = factory.createProjection(CategoryTotalDTO.class, source);


            List<CategoryTotalDTO> categoryTotalDTOS = List.of(categoryTotalDTO);

            CategoryFilterDTO filter = new CategoryFilterDTO("day");

            given(expenseService.estimateCategoryTotal(filter, mockUser.getId()))
                    .willReturn(categoryTotalDTOS);

            mockMvc.perform(get("/expenses/categories-total")
                            .param("filter","day"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].category").value("Food"))
                    .andExpect(jsonPath("$[0].total").value(1899.00));

            verify(expenseService).estimateCategoryTotal(filter, mockUser.getId());
        }

        @Test
        void shouldReturnEmptyList_whenNoExpensesExistForProvidedFilter() throws Exception{
            List<CategoryTotalDTO> categoryTotalDTOS = List.of();

            CategoryFilterDTO filterType = new CategoryFilterDTO("day");

            given(expenseService.estimateCategoryTotal(filterType, mockUser.getId()))
                    .willReturn(categoryTotalDTOS);

            mockMvc.perform(get("/expenses/categories-total")
                    .param("filter", "day"))
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());

            verify(expenseService).estimateCategoryTotal(filterType, mockUser.getId());
        }

        @Test
        void shouldReturnBadRequest_whenFilterParamIsInvalid() throws Exception{

            mockMvc.perform(get("/expenses/categories-total")
                    .param("filter", "abc"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.filter").value("Filter must be 'day', 'week', or 'month'"));

            verify(expenseService, never()).estimateCategoryTotal(any(),anyLong());
        }

        @Test
        void shouldReturnBadRequest_whenFilterParamDoesNotExist() throws Exception{
            mockMvc.perform(get("/expenses/categories-total"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.filter").value("Filter can not be null"));

            verify(expenseService, never()).estimateCategoryTotal(any(),anyLong());
        }


    }

    @Nested
    class addExpense{
        ExpenseReqDTO expenseReqDTO = new ExpenseReqDTO(
                BigDecimal.valueOf(50),
                1L,
                "Lunch at break",
                LocalDate.of(2026, 8, 8),
                LocalTime.of(12, 0),
                "cash");



        @Test
        void shouldReturnStatusCreated_whenRequestIsValid() throws Exception{
            String idempotency_key = "abc123456789";
            AddExpenseResDTO addExpenseResDTO = new AddExpenseResDTO("201");

            given(expenseService.addExpense(expenseReqDTO, mockUser.getId(),idempotency_key))
                    .willReturn(addExpenseResDTO);

            mockMvc.perform(post("/expenses/add_expense")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .header("Idempotency-Key", idempotency_key)
                    .content("""
                            {
                                "amount":50,
                                "categoryId": 1,
                                "description": "Lunch at break",
                                "date":"2026-08-08",
                                "time":"12:00:00",
                                "payment":"cash"
                            }
                    """)
            )
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.status").value("201"));

            verify(expenseService).addExpense(expenseReqDTO, mockUser.getId(), idempotency_key);
        }

        @Test
        void shouldReturnBadRequest_whenIdempotencyKeyHeaderIsInvalid() throws Exception{
            String idempotency_key = "abc";
            mockMvc.perform(post("/expenses/add_expense")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("Idempotency-Key", idempotency_key)
                            .content("""
                            {
                                "amount":50,
                                "categoryId": 1,
                                "description": "Lunch at break",
                                "date":"2026-08-08",
                                "time":"12:00:00",
                                "payment":"cash"
                            }
                    """)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message['addExpense.idempotencyKey']").value("The Idempotency Key length must be between 10 and 50 characters"))
                    .andExpect(jsonPath("$.localDate").exists());

            verify(expenseService, never()).addExpense(any(), anyLong(), any());
        }

        @Test
        void shouldReturnBadRequest_whenRequestBodyHasMissingField() throws Exception{
            String idempotency_key = "abc123456789";

            mockMvc.perform(post("/expenses/add_expense")
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .header("Idempotency-Key", idempotency_key)
                            .content("""
                            {
                                "categoryId": 1,
                                "description": "Lunch at break",
                                "date":"2026-08-08",
                                "time":"12:00:00",
                                "payment":"cash"
                            }
                    """)
                    )
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.amount").value("Amount is required"))
                    .andExpect(jsonPath("$.localDate").exists());

            verify(expenseService, never()).addExpense(any(), anyLong(), any());
        }

    }

    @Nested
    class updateExpense{

        @Test
        void shouldReturnStatusOk_whenValidRequestBody() throws Exception{
           ExpenseResDTO expenseResDTO = new ExpenseResDTO(
                    1L,
                    BigDecimal.valueOf(50),
                    "Lunch during work",
                    LocalDateTime.of(2026,8,8,12,0,0),
                    "cash",
                    1L,
                    "Food"
            );

            ExpenseReqDTO expenseReqDTO = new ExpenseReqDTO(
                    BigDecimal.valueOf(50),
                    1L,
                    "Lunch during work",
                    LocalDate.of(2026, 8, 8),
                    LocalTime.of(12, 0, 0),
                    "cash"
            );

            given(expenseService.updateExpense(expense.getId(), expenseReqDTO , mockUser.getId()))
                    .willReturn(expenseResDTO);

            mockMvc.perform(put("/expenses/update_expense/{expenseId}", expense.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                       "amount": 50,
                                       "categoryId": 1,
                                       "description": "Lunch during work",
                                       "date": "2026-08-08",
                                       "time": "12:00:00",
                                       "payment": "cash"
                                    }
                                    """))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.payment").value("cash"))
                    .andExpect(jsonPath("$.description").value("Lunch during work"));

            verify(expenseService).updateExpense(expense.getId(), expenseReqDTO, mockUser.getId());
        }

        @Test
        void shouldReturnBadRequest_whenRequestBodyFieldIsMissing() throws Exception{
            mockMvc.perform(put("/expenses/update_expense/{expenseId}", expense.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("""
                                    {
                                       "categoryId": 1,
                                       "description": "Lunch during work",
                                       "date": "2026-08-08",
                                       "time": "12:00:00",
                                       "payment": "cash"
                                    }
                                    """))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value(400))
                    .andExpect(jsonPath("$.message.amount").value("Amount is required"));


            verify(expenseService, never()).updateExpense(any(), any(), anyLong());
        }

        @Test
        void shouldReturnNotFound_whenExpenseDoesNotExist() throws Exception{
            Long invalidExpenseId = 999L;

            ExpenseReqDTO expenseReqDTO = new ExpenseReqDTO(
                    BigDecimal.valueOf(50),
                    1L,
                    "Lunch during work",
                    LocalDate.of(2026, 8, 8),
                    LocalTime.of(12, 0, 0),
                    "cash"
            );

            given(expenseService.updateExpense(invalidExpenseId, expenseReqDTO, mockUser.getId()))
                    .willThrow(new ExpenseNotFoundException(invalidExpenseId));

            mockMvc.perform(put("/expenses/update_expense/{expenseId}", invalidExpenseId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("""
                                    {  "amount": 50,
                                       "categoryId": 1,
                                       "description": "Lunch during work",
                                       "date": "2026-08-08",
                                       "time": "12:00:00",
                                       "payment": "cash"
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Expense not found"));


            verify(expenseService).updateExpense( invalidExpenseId, expenseReqDTO, mockUser.getId());
        }

        @Test
        void shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception{
            Long invalidCategoryId = 999L;

            ExpenseReqDTO expenseReqDTO = new ExpenseReqDTO(
                    BigDecimal.valueOf(50),
                    999L,
                    "Lunch during work",
                    LocalDate.of(2026, 8, 8),
                    LocalTime.of(12, 0, 0),
                    "cash"
            );

            given(expenseService.updateExpense(expense.getId(), expenseReqDTO, mockUser.getId()))
                    .willThrow(new CategoryNotFoundException(invalidCategoryId));

            mockMvc.perform(put("/expenses/update_expense/{expenseId}", expense.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON)
                            .content("""
                                    {  "amount": 50,
                                       "categoryId": 999,
                                       "description": "Lunch during work",
                                       "date": "2026-08-08",
                                       "time": "12:00:00",
                                       "payment": "cash"
                                    }
                                    """))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Category with ID 999 was not found!"));


            verify(expenseService).updateExpense( expense.getId(), expenseReqDTO, mockUser.getId());
        }
    }

    @Nested
    class deleteExpense{

        @Test
        void shouldReturnNoContent_whenExpenseExists() throws Exception{
            doNothing().when(expenseService).deleteExpense(expense.getId(), mockUser.getId());

            mockMvc.perform(put("/expenses/delete-expense/{expenseId}", expense.getId()))
                    .andExpect(status().isNoContent());

            verify(expenseService).deleteExpense(expense.getId(), mockUser.getId());
        }

        @Test
        void shouldReturnNotFound_whenExpenseDoesNotExist() throws Exception{
            Long invalidExpenseId = 999L;

            doThrow(new ExpenseNotFoundException(invalidExpenseId))
                    .when(expenseService).deleteExpense(invalidExpenseId, mockUser.getId());

            mockMvc.perform(put("/expenses/delete-expense/{expenseId}", invalidExpenseId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.message").value("Expense not found"));

            verify(expenseService).deleteExpense(invalidExpenseId, mockUser.getId());
        }

        @Test
        void shouldReturnNotFound_whenCategoryDoesNotExist() throws Exception{
            Long invalidCategoryId = 999L;

            doThrow(new CategoryNotFoundException(invalidCategoryId))
                    .when(expenseService).deleteExpense(expense.getId(), mockUser.getId());

            mockMvc.perform(put("/expenses/delete-expense/{expenseId}", expense.getId()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404))
                    .andExpect(jsonPath("$.message").value("Category with ID 999 was not found!"));


            verify(expenseService).deleteExpense(expense.getId(), mockUser.getId());
        }
    }
}
