import React from 'react';
import { useQuery } from '@tanstack/react-query';
import api from '../axiosClientApi/axios';

const useCategories = ({chosenFilter}) => {
    const getCategoriesTotal = async () => {
        const res = await api.get(`expenses/categories-total?filter=${chosenFilter}`);
        return res.data.sort((a, b) => b.total - a.total);
    }


    const { data: categoriesTotal = [] , isLoading: totalIsLoading } = useQuery({
        queryKey: ["category-total", chosenFilter],
        queryFn: getCategoriesTotal,
        enabled: !!chosenFilter
    })

    return {
    categoriesTotal,
    totalIsLoading
  }
}

export default useCategories