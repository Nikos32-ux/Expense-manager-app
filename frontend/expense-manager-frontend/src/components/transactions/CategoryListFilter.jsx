import React, { useState } from 'react';
import { fetchCategories } from '../../api/fetchCategories';
import { useQuery } from '@tanstack/react-query';
import { categories } from '../../categories/categories';

const CategoryListFilter = ({ setOpenPicker, setTransactionFilters }) => {
    const [choice, setChoice] = useState(null);

    return (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-end">
            <div className="w-full bg-slate-900 rounded-t-[30px] p-6 animate-slide-up">
                <h2 className="text-white font-bold mb-4">Select Category</h2>
                <div className="grid grid-cols-3 gap-3">
                    {categories.map((cat, idx) => {
                        return <button
                            key={idx}
                            onClick={() => setChoice(prev => prev !== idx ? idx : prev)}
                            className={`${choice === idx ? "bg-white/30" : "bg-white/10"} py-2 rounded-lg text-white text-sm`}>
                            {cat.name}
                        </button>
                    })}
                </div>
                <div className="btns flex items-center justify-center mt-6 gap-6">
                    <button onClick={() => setOpenPicker(false)} className=" py-3 text-gray-400">Cancel</button>
                    {choice !== null && <button onClick={() => {
                        setTransactionFilters(prev => ({ ...prev, category: {...prev.category, id: categories[choice].id, name: categories[choice].name } }));
                        setOpenPicker(false);
                    }
                    } className=' py-3 text-gray-400'>Apply</button>
                    }
                </div>
            </div>
        </div>
    )
}

export default CategoryListFilter