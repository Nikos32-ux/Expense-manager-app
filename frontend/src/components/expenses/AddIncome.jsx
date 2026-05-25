import React, { useEffect, useRef, useState } from 'react';
import { useNavigate, Form, useActionData, useNavigation } from 'react-router-dom';
import api from '../../axiosClientApi/axios';
import { queryClient } from '../../context/queryClient';
import { v4 as uuidv4 } from 'uuid';


const AddIncome = () => {
    const data = useActionData();
    const navigation = useNavigation();
    const [idempotencyKey] = useState(() => uuidv4());
    const navigate = useNavigate();
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(false);
    const startProcessRef = useRef(false);

    useEffect(() => {
        if (navigation.state === "submitting") {
            startProcessRef.current = false;
            return;
        }

        if (!data) return;

        let successTimer;
        let errorTimer;
        let navTimer;

        if (data?.success && startProcessRef.current === false) {
            startProcessRef.current = true;
            setSuccess(data?.success);
            queryClient.invalidateQueries({ queryKey: ["month-income-total"] });
            successTimer = setTimeout(() => {
                setSuccess(false);
            }, 4000);

            navTimer = setTimeout(() => {
                navigate("/dashboard");
            }, 2000);
        } else if (data?.error) {
            setError(data?.error);

            errorTimer = setTimeout(() => {
                setError(false);
            }, 2000);
        }

        return () => {
            clearTimeout(successTimer);
            clearTimeout(navTimer);
            clearTimeout(errorTimer);
        }

    }, [navigation, data])



    return (
        <div className='expense-detail-container fixed inset-0 lg:left-64 lg:max-w-auto bg-black/40 backdrop-blur-md z-50 flex justify-center items-center openExpenseDetail'>
            <div className='expense-detail-modal border border-white/30 shadow-lg bg-black/20 opacity-1 backdrop-blur-2xl rounded-md h-[80%] w-[90%] flex flex-col'>
                <div className="header relative h-[20vh] w-full bg-gradient-to-b from-blue-600 to-black/20 rounded-b-3xl flex flex-col items-start">
                    <div>
                        <span
                            onClick={() => navigate("/dashboard")}
                            className='text-white font-bold text-2xl mx-2 cursor-pointer hover: text-gray-400'>
                            x
                        </span>
                    </div>
                    <div className=' p-2 mx-2 mt-2'>
                        <p className='text-sm text-gray-400/80'>Income</p>
                        <p className='text-2xl text-white font-bold leading-tight tracking-widest'>Add Income</p>
                    </div>
                </div>
                <div className="add-expense-form w-full flex justify-center items-center flex-1">

                    <Form method="post" className=' h-full flex flex-col w-[80%] text-start gap-5 p-5'>
                        {
                            error &&
                            <div className='w-[80%] mx-auto rounded-md py-2 px-2 bg-red-600/20 backdrop-blur-xl text-center'>
                                <p className=' text-white text-[18px] font-semibold'>{error}</p>
                            </div>
                        }
                        {
                            success &&
                            <div className='w-[80%] mx-auto rounded-md py-2 px-2 bg-green-600/20 backdrop-blur-xl text-center'>
                                <p className=' text-white text-[18px] font-semibold'>{success}</p>
                            </div>
                        }
                        <div className='flex flex-col justify-start p-2 rounded-sm w-full'>
                            <label htmlFor="amount" className='text-md text-blue-400'>Amount</label>
                            <input
                                name="amount"
                                type="number"
                                id="amount"
                                step="0.01"
                                placeholder='0.00'
                                className='h-full w-full bg-white/5 text-slate-100 p-2 border border-blue-400/20 rounded-sm'
                            />
                        </div>
                        <div className='flex flex-col justify-start p-2 rounded-sm w-full'>
                            <label className='text-md text-blue-400' htmlFor="">Source of Income</label>
                            <select name="source" className='h-full w-full bg-white/5 text-gray-800 p-2 border border-blue-400/20 rounded-sm'>
                                <option value="">CATEGORY</option>
                                <option value="salary">SALARY</option>
                                <option value="freelance">FREELANCE</option>
                                <option value="investment">INVESTMENT</option>

                            </select>
                        </div>
                        <div className='flex flex-col justify-start p-2 rounded-sm w-full'>
                            <label htmlFor="date" className='text-md text-blue-400'>Date</label>
                            <input
                                name="date"
                                type="date"
                                id="date"
                                className='h-full w-full border border-blue-400/20 bg-white/5 text-slate-100 p-2 rounded-sm'
                            />
                        </div>
                        <div>
                            <input type="hidden" name="idempotencyKey" defaultValue={idempotencyKey} />
                        </div>
                        <div className='flex justify-center p-2'>
                            <button
                                disabled={navigation.state === "submitting" ? true : false}
                                type='submit'
                                className={`${navigation.state === "submitting" ? "bg-blue-400/30" : "bg-blue-500"} shadow-2xl  text-white text-md active:scale-95 hover:bg-blue-600 p-2 rounded-md`}>
                                {navigation.state === "submitting" ? "Saving income" : "Save Income"}
                            </button>
                        </div>
                    </Form>
                </div>
            </div>
        </div>
    )
}


export const addIncomeAction = async ({ request }) => {
    const data = await request.formData();

    const amount = data.get("amount");
    const date = data.get("date");
    const source = data.get("source")?.toUpperCase();
    const key = data.get("idempotencyKey");
  
    

    if (!amount || !date || !source) return { error: "All fields are required" };
    if (Number(amount) < 0) return { error: "Insert a positive amount" };

    try {
        const res = await api.post("/income/add-income", { amount, date, source },{
             headers: {
              'Idempotency-Key': key
            }
        });
        return { success: "Income added successfully!" };
    }
    catch (error) {
        return { error: "Add income failed" }
    }
}

export default AddIncome