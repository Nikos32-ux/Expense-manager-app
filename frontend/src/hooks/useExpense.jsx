import React, { useContext } from 'react'
import { useMutation, useQuery } from '@tanstack/react-query';
import api from '../axiosClientApi/axios';
import { queryClient } from '../context/queryClient';
import { useNavigate } from 'react-router-dom';
import { ExpenseDetailContext } from '../context/ExpenseDetailContext';

const useExpense = (id) => {
    const navigate = useNavigate();
    const {reportStale, setReportStale} = useContext(ExpenseDetailContext);
    const saveChanges = async ({ id, data }) => {
        const indexOfDelimiter = data.date.indexOf("T");
        const updateData = {
            amount: parseFloat(data.amount),
            categoryId: parseInt(data.category.id),
            description: data.desc,
            date: data.date.substring(0, indexOfDelimiter),
            time: data.date.substring(indexOfDelimiter + 1),
            payment: data.payment
        }
        
        const res = await api.put(`/expenses/update_expense/${id}`, updateData);
        return res.data;
    }

    const { mutate: updateExpense, isPending: updatePending, isSuccess: updateSuccess } = useMutation({
        mutationFn: saveChanges,
        onSuccess: () => {
            setReportStale(true);
            queryClient.invalidateQueries(["expense", id]);
            queryClient.invalidateQueries({ queryKey: ["dashboard-expenses"] });
            queryClient.invalidateQueries({ queryKey: ["expenses"] });
            queryClient.invalidateQueries({ queryKey: ["monthly-expense-total"] });
        }
    })


    const softDeleteExpense = async (id) => {
        const res = await api.put(`/expenses/delete-expense/${id}`);
        return res.data;
    }

    const { mutate: expenseDelete, isPending: expenseDeletePending, isSuccess: expenseDeleteSuccess } = useMutation({
        mutationFn: softDeleteExpense,
        onSuccess: () => {
            navigate("/dashboard", { state: { deleted: true } });
            setReportStale(true);
            queryClient.removeQueries({ queryKey: ["expense", id] });
            queryClient.invalidateQueries({ queryKey: ["dashboard-expenses"] });
            queryClient.invalidateQueries({ queryKey: ["expenses"] });
            queryClient.invalidateQueries({ queryKey: ["monthly-expense-total"] });
        }
    })

    const getExpenseDetails = async () => {
        const res = await api.get(`/expenses/get-expense/${id}`);
        return res.data;
    }

    const { data: expense, isError: expenseError, isLoading: expenseLoading } = useQuery({
        queryKey: ["expense", id],
        queryFn: getExpenseDetails,
        enabled: !expenseDeleteSuccess
    })


    return {
        expense,
        expenseError,
        expenseLoading,
        updateExpense,
        updatePending,
        updateSuccess,
        expenseDelete,
        expenseDeleteSuccess
    };
}

export default useExpense



