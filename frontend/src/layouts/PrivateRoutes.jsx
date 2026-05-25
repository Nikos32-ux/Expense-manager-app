import { useIsRestoring, useQuery } from '@tanstack/react-query';
import React from 'react'
import { Navigate, Outlet } from 'react-router-dom';
import { verifyUser } from '../queries/authQuery';
import RootErrorBoundary from '../components/ui/RootErrorBoundary';
import LoadSpinner from '../components/ui/LoadSpinner';


const PrivateRoutes = () => {
    const isRestoring = useIsRestoring();
    const {data: user, isError, error} = useQuery(verifyUser());
    
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

    return <Outlet/>;
}

export default PrivateRoutes