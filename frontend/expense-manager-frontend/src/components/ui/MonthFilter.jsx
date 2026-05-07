import React from 'react'
import { LuChevronDown } from 'react-icons/lu'

const MonthFilter = ({transactionFilters, setTransactionFilters }) => {
  return (
                <div className="flex  flex-col items-center">
                  {transactionFilters?.month
                    ? (
    
                      <div className="flex min-w-auto items-center gap-1 bg-white/30 text-gray-800 backdrop-blur-sm text-sm font-bold px-5 py-2 rounded-lg border-white/40 cursor-pointer">
                        <span>{transactionFilters.month}</span>
                        <button
                          onClick={() => setTransactionFilters(prev => ({ ...prev, month: "" }))}
                          className="text-gray-800 hover:text-red-500"
                        >
                          ×
                        </button>
                      </div>
    
                    )
                    : (
                      <div data-id="1" className="flex items-center gap-1 bg-white/30 text-gray-800 backdrop-blur-sm text-sm font-bold px-5 py-2 rounded-lg border-white/40 cursor-pointer">
                        Month <LuChevronDown size={14} className="text-gray-800" />
                      </div>
                    )
                  }
    
    
                </div>
  )
}

export default MonthFilter