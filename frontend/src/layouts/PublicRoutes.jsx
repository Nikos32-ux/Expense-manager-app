import React from 'react'
import { Navigate, Outlet } from 'react-router-dom'
import { queryClient } from '../context/queryClient';
import LoadSpinner from '../components/ui/LoadSpinner';
import { useIsRestoring } from '@tanstack/react-query';

const PublicRoutes = () => {
  const isRestoring = useIsRestoring();
  
  if(isRestoring) {
    return <LoadSpinner/>
  }
  
  const user = queryClient.getQueryData(["verification"]);
  
  if(user) return <Navigate to="/dashboard" replace />

  return <Outlet/>;
}

export default PublicRoutes