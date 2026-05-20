  import React, { useContext, useEffect, useRef, useState } from 'react';
  import { Form, useActionData, useNavigate, useNavigation } from 'react-router-dom';
  import api from '../../axiosClientApi/axios';
  import { fetchCategories } from '../../api/fetchCategories';
  import { useQuery } from '@tanstack/react-query';
  import { useTranslation } from 'react-i18next';
  import { LuBanknote } from 'react-icons/lu'
  import { queryClient } from '../../context/queryClient';
  import { ExpenseDetailContext } from '../../context/ExpenseDetailContext';
  import { v4 as uuidv4 } from 'uuid';


  const AddExpense = () => {
    const data = useActionData();
    const textareaRef = useRef(null);
    const {t} = useTranslation();
    const { reportStale, setReportStale } = useContext(ExpenseDetailContext);
    const [idempotencyKey] = useState(()=> uuidv4());
    const [desc, setDesc] = useState("");
    const [error, setError] = useState(null);
    const [success, setSuccess] = useState(null);
    const navigation = useNavigation();
    const navigate = useNavigate();
    const startProcessRef = useRef(false);


    useEffect(() => {
      if (!textareaRef) return;
      if (textareaRef && textareaRef.current) {
        const textareaEl = textareaRef.current;
        textareaEl.style.height = 0 + "px";
        textareaEl.style.height = textareaEl.scrollHeight + "px";
      }
    }, [desc]);

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
        setReportStale(true);

         queryClient.invalidateQueries(["month-expenses-total"]);
         queryClient.invalidateQueries({ queryKey: ["dashboard-expenses"] });
         queryClient.invalidateQueries({ queryKey: ["expenses"] });
        successTimer = setTimeout(() => { setSuccess(false); }, 4000);
        navTimer = setTimeout(() => { navigate("/dashboard"); }, 2000);

      } else if (data?.error) {
        setError(data?.error);
        errorTimer = setTimeout(() => { setError(false); }, 2000);
      }


      return () => {
        clearTimeout(successTimer);
        clearTimeout(navTimer);
        clearTimeout(errorTimer);
      }

    }, [navigation, data, navigate]);


    const { data: categories = [] } = useQuery({
      queryKey: ["expense-categories"],
      queryFn: fetchCategories
    })


    return (
      <div className='fixed inset-0 lg:left-64 lg:max-w-auto bg-black/40 backdrop-blur-md z-50 flex justify-center items-center openExpenseDetail'>
        <div className='expense-detail-modal border border-white/30 shadow-lg bg-black/20 opacity-1 backdrop-blur-2xl rounded-md h-[95%] w-[90%] flex flex-col'>
          <div className="header relative h-[20vh] w-full bg-gradient-to-b from-blue-600 to-black/20 rounded-b-3xl flex flex-col items-start">
            <div>
              <span
                onClick={() => navigate("/dashboard")}
                className={`text-white font-bold text-2xl mx-2 cursor-pointer hover: text-gray-400 ${navigation.state === "submitting" ? "pointer-events-none opacity-50" : "opacity-100"}`}>
                x
              </span>
            </div>
            <div className=' p-2 mx-2 mt-2'>
              <p className='text-sm text-gray-400/80'>{t("expense")}</p>
              <p className='text-2xl text-white font-bold leading-tight tracking-widest'>{t("add-expense")}</p>
            </div>
          </div>
          <div className="add-expense-form w-full flex justify-center bg-trasparent items-start overflow-y-auto">
            <Form method='post' className={`w-full max-w-lg space-y-2 ${navigation.state === "submitting" ? "opacity-50 pointer-events-none" : "opacity-100"} `}>
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
              <div className='flex flex-col gap-2 p-2'>
                <label htmlFor="date" className='text-md font-semibold uppercase tracking-wider text-blue-400 ml-1'>{t("transaction-date")}</label>
                <input
                  disabled={navigation.state === "submitting"}
                  name='monthYearDay'
                  type="date"
                  id="date"
                  className='w-full border border-white/10 bg-slate-900/40 text-slate-100 p-3 rounded-xl focus:border-blue-400 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none'
                />
                <input
                  disabled={navigation.state === "submitting"}
                  name='time'
                  type="time"
                  id="date"
                  className=' w-full border border-white/10 bg-slate-900/40 text-slate-100 p-3 rounded-xl focus:border-blue-400 focus:ring-4 focus:ring-blue-500/10 transition-all outline-none'
                />
              </div>
              <div className='flex flex-col justify-start p-2 rounded-sm w-full'>
                <label htmlFor="categoryId" className='text-md font-semibold uppercase tracking-wider text-blue-400 ml-1'>{t("category")}</label>
                <select
                  disabled={navigation.state === "submitting"}
                  name='categoryId'
                  type="categoryId"
                  id="categoryId"
                  className='w-full bg-slate-900/40 border border-white/10 text-slate-100 p-3 rounded-xl focus:border-blue-500 outline-none capitalize'
                >
                  {categories && categories.map((category) => {
                    return <option
                      key={category.id}
                      value={category.id}
                      className=''
                    >
                      {t(category.category)}
                    </option>
                  })}

                </select>
              </div>
              <div>
                <input type="hidden" name="idempotencyKey" value ={idempotencyKey} />
              </div>
              <div className='flex flex-col justify-start p-2 rounded-sm w-full'>
                <label htmlFor="desc" className='text-md  uppercase tracking-wider text-blue-400 ml-1'>{t("description")}</label>
                <textarea
                  disabled={navigation.state === "submitting"}
                  name='description'
                  ref={textareaRef}
                  value={desc}
                  onChange={(e) =>setDesc(e.target.value)}
                  type="text"
                  id='desc'
                  placeholder={t('add-description-expense')}
                  rows={1}
                  className='w-full bg-slate-900/40 border border-white/10 text-slate-100 p-4 rounded-xl focus:border-blue-500 outline-none resize-none max-h-[100px]' />
              </div>
              <div className='flex flex-col justify-start p-3 rounded-sm w-full'>
                <label htmlFor="amount" className='font-semibold uppercase tracking-wider text-blue-400 ml-1'>{t("amount")}</label>
                <input
                  disabled={navigation.state === "submitting"}
                  name='amount'
                  type="number"
                  id="amount"
                  step="0.01"
                  placeholder='0.00'
                  className='w-full bg-slate-900/40 border border-white/10 text-slate-100 p-3 pl-8 rounded-xl focus:border-blue-500 outline-none text-xl font-medium'
                />
              </div>
              <div className='flex flex-col justify-start p-2 rounded-sm w-full'>
                <div className="flex items-center gap-2 mb-1 ml-1">
                  <div className="p-1.5 rounded-lg bg-emerald-500/20">
                    <LuBanknote className="text-emerald-400" size={18} />
                  </div>
                  <label htmlFor="payment" className='text-md font-semibold uppercase tracking-wider text-blue-400 ml-1'>
                    {t("payment-method")}
                  </label>
                </div>
                <select
                  disabled={navigation.state === "submitting"}
                  name='payment'
                  id="payment"
                  className='w-full bg-slate-900/40 border border-white/10 text-slate-100 p-3 rounded-xl focus:border-blue-500 outline-none capitalize'
                >
                  <option value="cash" className="bg-slate-800">{t("cash")}</option>
                  <option value="card" className="bg-slate-800">{t("card")}</option>
                </select>

              </div>
              <div className='flex justify-center p-2'>
                <button
                  disabled={navigation.state === "submitting"}
                  type='submit'
                  className={`w-full ${navigation.state === "submitting" ? "bg-blue-400/30" : "bg-blue-600"} 
                  hover:bg-blue-500 text-white  font-bold py-4 rounded-2xl shadow-lg shadow-blue-500/20 
                  transition-all flex justify-center items-center gap-2`}>
                  {navigation.state === "submitting"
                    ? (
                      <span className='flex items-center justify-center gap-2'>
                        <span className='animate-spin h-4 w-4 border-2 border-white border-b-transparent rounded-full'></span>
                        {t("saving")}
                      </span>
                    )
                    : t("save-expense")
                  }
                </button>
              </div>
            </Form>
          </div>
        </div>
      </div>
    )
  }

  export const addExpenseAction = async ({ request }) => {
    const data = await request.formData();
    const date = data.get("monthYearDay");
    const time = data.get("time");
    const categoryId = parseInt(data.get("categoryId"));
    const description = data.get("description");
    const amount = parseFloat(data.get("amount"));
    const payment = data.get("payment");
    const requestKey = data.get("idempotencyKey");


    if (!amount || !date || !time || !categoryId || !description || !payment) return { error: "All fields are required" };
    if (amount < 0) return { error: "Insert a positive amount" };
    if (description.length > 255) return { error: "Description up to 255 characters" };
    
    const finalExpense = { amount, categoryId, description, date, time, payment };
    try {
      console.log("idempotency key", requestKey);
      
      const res = await api.post( 
        "/expenses/add_expense", finalExpense, {
            headers: {
              'Idempotency-Key': requestKey
            }
          });
      
        return { success: "Expense added successfully!" };
    }
    catch (error) {
      // TODO: add proper logging (EC2 / production debugging)
      console.error(error);
      return { error: "Add expense failed" }
    }
  }

  export default AddExpense