import React from 'react';
import { Link } from 'react-router-dom';
import { LuChevronRight } from 'react-icons/lu';
import * as LuIcons from "react-icons/lu";
import { useTranslation } from 'react-i18next';

const Expense = ({ expense, categoryRow }) => {
    const {t, i18n} = useTranslation();
    const activeLang = i18n.language === 'en' ? 'en-US' : 'el-GR';  
    return (
        <div className='expense w-[90%] mx-auto rounded-md px-4 flex justify-between items-center border-t gap-3 py-3 bg-white/10 backdrop-blur-md shadow-lg'>
            <div className='flex gap-3 items-center justify-center'>
                <div className={`${categoryRow.bg} w-8 h-8 flex items-center justify-center rounded-sm`}>
                   <categoryRow.icon size={24} className={categoryRow.text}/>
                </div>
                <div>
                    <p className={`font-bold text-gray-800  text-lg`}>{t(categoryRow.name)}</p>
                    <p className='text-gray-400 text-sm'>{new Date(expense.date).toLocaleString(
                        activeLang, { month: 'long', day: 'numeric' })}
                    </p>
                </div>
            </div>
            <div className='flex gap-1 items-center'>
                <p className='text-red-600 font-bold mr-2 text-md'>{expense.amount}</p>
                <Link to={`${expense.id}`}><LuChevronRight size={20} className='text-gray-500 cursor-pointer' /></Link>
            </div>
        </div>
    )
}

export default Expense