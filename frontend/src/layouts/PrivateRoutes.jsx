import { useIsRestoring, useQuery } from '@tanstack/react-query';
import React from 'react'
import { Navigate, Outlet } from 'react-router-dom';
import { verifyUser } from '../queries/authQuery';
import RootErrorBoundary from '../components/ui/RootErrorBoundary';
import LoadSpinner from '../components/ui/LoadSpinner';
import { runVerification } from '../queries/authQuery';

const PrivateRoutes = () => {
    const isRestoring = useIsRestoring();
    const {data: user, isError, error, isLoading} = useQuery({
        queryKey: ["verification"],
            queryFn: runVerification,
            staleTime: 1000 * 60 * 5,
            retry: false,
            refetchOnWindowFocus: false,
            meta: { persist: true },
            enabled:!!isRestoring
    });
    
    if(isRestoring) {
        return <LoadSpinner/>;
      }

    if(isError && error?.response?.status >= 500){
        return <RootErrorBoundary/>
    }

    if(isError && error?.response?.status === 401){
        return <Navigate to="/login" />
    }

    if(!user) return <Navigate to="/login" />

    return <Outlet context={{user, isLoading}}/>;
}

export default PrivateRoutes