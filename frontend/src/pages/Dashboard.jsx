import { Link, Outlet, useLoaderData, useLocation, useNavigate, useOutletContext, useRouteLoaderData } from 'react-router-dom';
import Navbar from '../components/ui/Navbar.jsx';
import { useTranslation } from 'react-i18next'
import AddExpense from '../components/expenses/AddExpense.jsx';
import api from '../axiosClientApi/axios';
import { queryClient } from '../context/queryClient';
import { useMutation, useQuery } from '@tanstack/react-query';
import { verifyUser } from '../queries/authQuery';
import Expense from '../components/expenses/Expense.jsx';
import useAuth from '../hooks/useAuth';
import * as LuIcons from "react-icons/lu";
import { LuUser, LuBell, LuChevronLeft, LuSettings2, LuShoppingBag, LuPlus, LuDownload, LuPanelTop, LuShieldCheck, LuLayoutGrid, LuWallet, LuFileUp, LuLogOut, LuFileText } from 'react-icons/lu';
import toast from 'react-hot-toast';
import LoadSpinner from '../components/ui/LoadSpinner.jsx';
import { categories } from '../categories/categories.js';
import { useContext, useEffect, useRef, useState } from 'react';
import ExpenseDetail from '../components/expenses/ExpenseDetail.jsx';
import useDashboard from '../hooks/useDashboard.jsx';
import ExpenseSkeleton from '../components/expenses/ExpenseSkeleton.jsx';
import { WebSocketContext } from '../context/WebsocketContext.jsx';
import { Client } from '@stomp/stompjs';


const DashBoard = () => {

  const { user, isLoading } = useOutletContext();
  const location = useLocation();
  const { notifications, setNotifications } = useContext(WebSocketContext);
  const { t, i18n } = useTranslation();
  const navigate = useNavigate();
  const mutation = useAuth();
  const [showDeletedSuccess, setShowDeletedSuccess] = useState(false);
  const toastRef = useRef(false);

  const {
    fetchedExpenses,
    expenseListIsLoading,
    monthExpensesData,
    monthExpensesDataIsLoading,
    monthIncomeData,
    monthIncomeDataIsLoading
  } = useDashboard();


  useEffect(() => {
    if (!location?.state?.success) return;
     if(location.state.success && !toastRef.current){
           toastRef.current = true;
           toast.success(location.state.success);
           navigate("/dashboard", { replace: true, state: {} });
        }
  }, [location.state?.success]);

  const renderSkeletons = new Array(5).fill(null).map((_, i) => <ExpenseSkeleton key={i} />);


  const renderExpenses = () => {
    if (fetchedExpenses.length === 0) return (
      <div className="flex bg-gray-200/10 flex-col items-center justify-center mt-20 opacity-80">
            <div className="bg-gray-200 p-6 rounded-full mb-4">
              <LuShoppingBag size={48} className="text-gray-400" />
            </div>
            <h3 className="text-gray-600 font-bold text-lg">{t("zero-transactions")}</h3>
            <p className="text-gray-500 text-sm text-center px-10">
              {t("dashboard-empty-subtitle")}
            </p>
          </div>)
    return fetchedExpenses.map((expense) => {
      let categoryRow = categories.find(category => category.id === expense.categoryId);
      return <Expense
        key={expense.id}
        categoryRow={categoryRow}
        expense={expense}
      />
    })
  }



  const estimateBalance = () => {
    let balance;
    const totalLoading = monthExpensesDataIsLoading || monthIncomeDataIsLoading;
    const dataNotExist = !monthExpensesData || !monthIncomeData;
    if (totalLoading) return;
    if (dataNotExist) return;

    balance = monthIncomeData.incomeTotal - monthExpensesData.amount;
    return balance.toFixed(2);
  }

  return (
    <>
      <div className='h-[100dvh] flex flex-col overflow-hidden lg:flex-row bg-white'>

        <aside className="hidden lg:flex lg:w-72 lg:flex-col bg-slate-950 text-white shrink-0 border-r border-white/5 relative overflow-hidden 
                            bg-gradient-to-b from-slate-950 via-slate-950 to-slate-900
                            shadow-2xl shadow-black/40">
          <div className="absolute inset-0 opacity-20">
            <div className="absolute top-[-100px] left-[-100px] w-[300px] h-[300px] bg-blue-500 blur-[120px]" />
          </div>
          <div className="flex flex-col h-full relative z-10">
            <div className="app-logo p-8">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-blue-600 rounded-xl flex items-center justify-center shadow-lg shadow-blue-500/20">
                  <span className="text-xl font-black italic">M</span>
                </div>
                <span className="text-xl font-bold tracking-tight">Moni<span className="text-blue-500">App</span></span>
              </div>
            </div>
            <nav className="flex-1 px-4 flex flex-col gap-2">
              <p className="text-[10px] uppercase tracking-[0.2em] text-gray-500 font-bold mb-2 ml-4">Menu</p>
              <div className=' flex flex-col gap-5 py-5 w-full items-start'>
                <Link to="/dashboard" className="group w-full flex items-center gap-3 p-3 py-4 rounded-xl transition-all hover:bg-white/5 hover:translate-x-1 hover:shadow-md">
                  <LuPanelTop size={22} className="text-gray-500 group-hover:text-blue-500 transition-colors" />
                  <p className="text-sm text-gray-400 group-hover:text-white transition-colors">
                    {t("dashboard")}
                  </p>
                </Link>
                <Link to="/transactions" className="group flex w-full items-center gap-3 p-3 py-4 rounded-xl transition-all hover:bg-white/5 hover:translate-x-1 hover:shadow-md">
                  <LuWallet size={22} className="text-gray-500 group-hover:text-blue-500 transition-colors" />
                  <p className="text-sm text-gray-400 group-hover:text-white transition-colors">
                    {t("expenses")}
                  </p>
                </Link>
                <Link to="/categories" className="group flex w-full items-center gap-3 p-3 py-4 rounded-xl transition-all hover:bg-white/5 hover:translate-x-1 hover:shadow-md">
                  <LuLayoutGrid size={22} className="text-gray-500 group-hover:text-blue-500 transition-colors" />
                  <p className="text-sm text-gray-400 group-hover:text-white transition-colors">
                    {t("categories")}
                  </p>
                </Link>
                <Link to="/profile" className="group flex w-full items-center gap-3 p-3 py-4 rounded-xl transition-all hover:bg-white/5 hover:translate-x-1 hover:shadow-md">
                  <LuUser size={22} className="text-gray-500 group-hover:text-blue-500 transition-colors" />
                  <p className="text-sm text-gray-400 group-hover:text-white transition-colors">
                    {t("profile")}
                  </p>
                </Link>
              </div>
            </nav>
            <div className="p-6 mt-auto border-t border-white/5">
              <button className="flex items-center gap-3 w-full p-3 text-gray-400 hover:text-red-400 hover:bg-red-500/10 rounded-xl transition-all group">
                <LuLogOut size={20} className="group-hover:translate-x-1 transition-transform" />
                <span className="font-semibold">Logout</span>
              </button>
            </div>
          </div>
        </aside>
        <div className="flex flex-col h-full">
          <div className='header pt-2 bg-[linear-gradient(to_bottom,rgba(0,0,0,0.2),rgba(0,0,0,0.6)),url("/login-page.jpg")] bg-cover bg-center h-[30vh] max-h-[25vh]'>
            {isLoading
              ? (
                <div className="flex justify-between px-5 mt-5 animate-pulse">
                  <div className="flex items-center gap-3">
                    <div className="w-12 h-12 rounded-full bg-gray-300/30" />
                    <div className="h-4 w-32 bg-gray-300/30 rounded" />
                  </div>

                  <div className="flex gap-2">
                    <div className="w-10 h-10 bg-gray-300/30 rounded" />
                    <div className="w-10 h-10 bg-gray-300/30 rounded" />
                  </div>
                </div>
              )
              : (
                <div className=' flex justify-between px-5  mt-5'>
                  <div className='flex justify-between items-center'>
                    <div className='w-12 h-12 border-2 border-blue-500 rounded-full'>
                      <img src={user?.imageProfile} className=' w-full h-full rounded-full object-cover' alt="" />
                    </div>
                    <p className='text-blue-400 ml-3 w-[120px] font-bold tracking-wider text-sm'>{t("welcome-back")}, <span className='text-white text-lg leading-tight'>{user?.username}</span></p>
                  </div>
                  <div className='flex items-center gap-1'>
                    <button onClick={() => {
                      setNotifications(prev => prev.map(n => ({ ...n, isRead: true })))
                    }} className='bg-blue-500/20 p-2 hover:bg-blue-700/20 relative'>
                      <LuBell size={24} className='text-blue-400 hover:text-white' />
                      {notifications.filter(not => !not.isRead).length > 0 && (
                        <span
                          className='w-5 h-5 text-sm font-semibold rounded-full bg-red-600 absolute flex items-center justify-center top-0 -right-0'>
                          {notifications.filter(not => !not.isRead).length}
                        </span>
                      )}

                    </button>
                    <button onClick={() => mutation.mutate()} className='bg-blue-500/20 p-2 hover:bg-blue-700/20'>
                      <LuLogOut size={24} className='text-blue-400  hover:text-white' />
                    </button>
                  </div>
                </div>
              )
            }

          </div>
          <div className="card bg-blue-300/20 w-[90%] mx-auto lg:max-w-4xl items-start rounded-xl backdrop-blur-md shadow-2xl -mt-20 mb-2">
            {showDeletedSuccess &&
              (<div className='w-[70%] mx-auto bg-green-600 p-2 rounded-md shadow-lg fadeInSuccess'>
                <h1 className='text-white font-semibold text-lg'>{t("delete-success")}
                </h1>
              </div>
              )
            }
            <div className='flex flex-col items-start'>
              <h1 className='text-white font-bold text-start mt-3 ml-3'>{t("total-balance")}</h1>
              <p className='text-white text-2xl leading-tight  ml-3 font-semibold '>${estimateBalance()}</p>
            </div>
            <div className='flex items-center justify-between p-4'>
              <div className='income text-white flex items-center gap-2'>
                <div>
                  <p className='text-xs uppercase tracking-widest text-gray-800/20 font-medium'>{t("income")}</p>
                  <p className='text-lg font-bold text-green-600/60'>
                    {monthIncomeDataIsLoading || !monthIncomeData
                      ? <span className=" w-20 h-6 bg-gray-300/30 animate-pulse rounded" />
                      : `$${monthIncomeData.incomeTotal}`}
                  </p>
                </div>
                <Link to={"add-income"}>
                  <LuPlus size={24} className='bg-green-500/20 text-green-500/30 border border-green-500/50 hover:bg-green-400 transition-colors hover:text-white cursor-pointer ml-2 rounded-full' />
                </Link>
              </div>
              <div className='expense text-white flex items-center gap-2'>
                <div>
                  <p className='text-xs uppercase tracking-widest  text-gray-800/20 font-medium'>{t("expenses")}</p>
                  <p className='text-lg font-bold text-red-600/60'>
                    {monthExpensesDataIsLoading || !monthExpensesData
                      ? <span className=" w-20 h-6 bg-gray-300/30 animate-pulse rounded" />
                      : `$${monthExpensesData.amount}`}</p>
                </div>
                <Link to={"add-expense"}>
                  <LuPlus size={24} className='bg-red-500/30 text-red-500/30 border border-red-500/20 cursor-pointer hover:bg-red-400 transition-colors hover:text-white ml-2 rounded-full' />
                </Link>
              </div>
            </div>
          </div>
          <div className='transaction-container bg-gray-100/10 shadow-md flex-1 rounded-t-lg flex flex-col min-h-0 mb-12 '>
            <div className='transaction-render '>
              <div className='flex items-center justify-between px-2'>
                <h1 className='text-gray-800/80 font-semibold text-md tracking-widest italic ml-2'>{t("recent-transactions")}</h1>
                <Link to={"/transactions"} className='text-gray-500 font-medium text-sm hover:text-blue-600 transition-colors mr-2'>{t("view-all")}</Link>
              </div>
            </div>
            <div className={`expenses-list w-full mt-4 flex flex-1 flex-col gap-3 pb-3 overflow-y-auto`}>
              {expenseListIsLoading ? renderSkeletons : renderExpenses()}
            </div>
          </div>
          <div className=" fixed bottom-0 right-0 left-0 z-50 lg:hidden">
            <Navbar />
          </div>
        </div>
        <Outlet context={{ categories }} />
      </div>
    </>
  )
}

export default DashBoard                                                            