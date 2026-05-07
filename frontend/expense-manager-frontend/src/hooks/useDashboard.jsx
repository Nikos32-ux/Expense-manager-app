import React from 'react';
import api from "../axiosClientApi/axios";
import { useQuery } from '@tanstack/react-query';

const useDashboard = () => {
    const fetchDashboardExpenses = async () => {
        const res = await api.get("/expenses/get-dashboard-expenses");
        return res.data.content;
    }

    const { data: fetchedExpenses = [], isLoading: expenseListIsLoading, isError: expensesListIsError } = useQuery({
        queryKey: ["dashboard-expenses"],
        queryFn: fetchDashboardExpenses
    })

    const monthExpenseTotal = async () => {
        const res = await api.get("/expenses/expense-month-total")
        return res.data;
    }

    const { data: monthExpensesData, isLoading: monthExpensesDataIsLoading } = useQuery({
        queryKey: ["month-expenses-total"],
        queryFn: monthExpenseTotal
    })

     const monthIncomeTotal = async () => {
         const res = await api.get("/income/income-month-total")
         return res.data;
    }

    const { data: monthIncomeData, isLoading: monthIncomeDataIsLoading } = useQuery({
        queryKey: ["month-income-total"],
        queryFn: monthIncomeTotal
    })


    return {
        fetchedExpenses,
        expenseListIsLoading,
        monthExpensesData,
        monthExpensesDataIsLoading,
        monthIncomeData,
        monthIncomeDataIsLoading   
    }
}

export default useDashboard;