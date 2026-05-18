import React from 'react'
import { useNavigate, useRouteError } from 'react-router-dom'

export default function RootErrorBoundary() {
  const error = useRouteError();
  const navigate = useNavigate();
  const status = error?.response?.status || error?.status;

  return (
    <div className='p-2 text-center'>
        <h1 className='text-[2rem] text-[#dc3545] font-bold'>
            Oops! Something went wrong
        </h1>
        <p className='my-4 text-gray-600'>An unexpected error occurred </p>
        <div className='flex gap-4 justify-center mt-4'>
            <button 
                className='px-4 py-2 bg-gray-200 rounded hover:bg-gray-300 transition' 
                onClick={() => window.location.reload()}
            >
                Reload page
            </button>
            <button
                className='px-4 py-2 border border-gray-300 rounded hover:bg-gray-50 transition' 
                onClick={() => navigate("/")}>
                Go to Welcome Page
            </button>
        </div>
    </div>
  )
}
