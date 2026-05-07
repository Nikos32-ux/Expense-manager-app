import React, { useState } from 'react'

const AmountListFilter = ({ setOpenPicker, setTransactionFilters }) => {
    const [sortByChoice, setSortByChoice] = useState(null);
    const [rangeSetChoice, setRangeSetChoice] = useState(null);

    const rangeSets = [
        { rangeSet: "<10", min: 0, max: 10 },
        { rangeSet: "10 - 20", min: 10, max: 20 },
        { rangeSet: "20 - 40", min: 20, max: 40 },
        { rangeSet: "40 -80", min: 40, max: 60 },
        { rangeSet: "60 - 80", min: 60, max: 80 },
        { rangeSet: "80 - 100", min: 80, max: 100 },
        { rangeSet: ">100", min: 100 },
    ];

    

    return (
        <div className="fixed inset-0 bg-black/60 z-50 flex items-end">
            <div className="w-full bg-slate-900 rounded-t-[30px] p-6 animate-slide-up">
                
                <div className="grid grid-cols-3 gap-3 mt-10">
                    {rangeSets.map((range, idx) => {
                        return <button
                                    key={idx}
                                    onClick={() => setRangeSetChoice(prev => prev !== idx ? idx : prev)} 
                                    className={`${rangeSetChoice === idx ? "bg-white/30" : "bg-white/10"} font-semibold py-2 rounded-lg text-white text-sm`}>
                                    {range.rangeSet}
                               </button>
                    })}
                </div>
                <div className="btns flex items-center justify-center mt-6 gap-6">
                    <button onClick={() => setOpenPicker(false)} className=" py-3 text-gray-400">Cancel</button>
                    {rangeSetChoice !== null && <button onClick={() => {
                        setTransactionFilters(prev => ({ ...prev,  amountMin: rangeSets[rangeSetChoice].min, amountMax: rangeSets[rangeSetChoice].max }));
                        
                        setOpenPicker(false);
                    }
                    } className=' py-3 text-gray-400'>Apply</button>
                    }
                </div>
            </div>
        </div>
    )
}

export default AmountListFilter