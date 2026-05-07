import React from 'react'
import { LuChevronDown } from 'react-icons/lu'

const CategoryFilter = ({ transactionFilters, setTransactionFilters }) => {
  return (
    <div className=' flex flex-col items-center left-1/2 transform-translate-y-1/2'>

      {transactionFilters?.category.name
        ? (

          <div className="flex items-center gap-1 bg-white/30 text-gray-800 backdrop-blur-sm text-sm font-bold px-5 py-2 rounded-lg border-white/40 cursor-pointer">
            <span>{transactionFilters.category.name}</span>
            <button
              onClick={() => setTransactionFilters(prev => ({ ...prev, category:{...prev.category, id:"", name: ""} }))}
              className="text-gray-800 hover:text-red-500"
            >
              ×
            </button>
          </div>

        )
        : (
          <div data-id="2" className="flex items-center gap-1 bg-white/30 text-gray-800 backdrop-blur-sm text-sm font-bold px-5 py-2 rounded-lg border-white/40 cursor-pointer">
            Category <LuChevronDown size={14} className="text-gray-800" />
          </div>
        )
      }

    </div>
  )
}

export default CategoryFilter