import React from 'react'

const ExpenseSkeleton = () => {
    return (
        <div className='expense w-[90%] mx-auto rounded-md px-4 flex justify-between items-center border-t gap-3 py-3 bg-white/10 backdrop-blur-md shadow-lg'>
            <div className='flex gap-3 items-center justify-center'>
                <div className={` w-8 h-8 bg-gray-300 flex items-center justify-center rounded-sm animate-pulse`}></div>
                <div>
                    <p className={`font-bold bg-gray-300 text-md animate-pulse`}></p>
                    <p className='bg-gray-200 text-sm animate-pulse'></p>
                </div>
            </div>
            <div className='flex gap-2 items-center'>
                <div className='w-12 h-3 bg-gray-300 rounded animate-pulse'></div>
                <div className='w-5 h-5 bg-gray-300 rounded-full animate-pulse'></div>
            </div>
        </div>
    )
}

export default ExpenseSkeleton