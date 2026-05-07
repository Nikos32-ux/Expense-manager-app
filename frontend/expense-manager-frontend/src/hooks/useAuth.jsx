import React from 'react';
import { useMutation } from '@tanstack/react-query';
import { useNavigate } from 'react-router-dom';
import api from '../axiosClientApi/axios';
import { queryClient } from '../context/queryClient';

 const useAuth = () => {
    const navigate = useNavigate();

    const logout = async() => {
        const res = await api.post("/auth/logout")
    }
    
    const mutation = useMutation({
        mutationFn: logout,
        onSuccess: () => {
            queryClient.clear();
            navigate("/");
        }
    })

    return mutation;
}

export default useAuth