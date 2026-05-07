import React, { useEffect, useReducer, useRef, useState, useContext } from 'react';
import { Link, Outlet, useLocation } from 'react-router-dom';
import { LuUtensils, LuChevronDown, LuChevronsUpDown, LuShoppingBag, LuCar, LuPanelTop, LuWallet, LuHeartPulse, LuLayoutGrid, LuLayoutDashboard, LuLogOut, LuUser, LuArrowLeftRight, LuZap, LuTv, LuChevronRight, LuX, LuAArrowDown, LuSquareArrowUp, LuAArrowUp, LuArrowUp, LuArrowDown } from 'react-icons/lu';
import Navbar from '../components/ui/Navbar';
import MonthListFilter from '../components/transactions/MonthListFilter';
import CategoryListFilter from '../components/transactions/CategoryListFilter';
import AmountListFilter from '../components/transactions/AmountListFilter';
import { useInfiniteQuery, useQueries, useQuery } from '@tanstack/react-query';
import MonthFilter from '../components/ui/MonthFilter.jsx';
import CategoryFilter from '../components/ui/CategoryFilter.jsx';
import api from '../axiosClientApi/axios.js';
import AmountFilter from '../components/ui/AmountFilter.jsx';
import Expense from '../components/expenses/Expense.jsx';
import { categories } from '../categories/categories.js';
import { ExpenseDetailContext } from '../context/ExpenseDetailContext.jsx';
import { useTranslation } from 'react-i18next';


const Transactions = () => {
  const { t } = useTranslation();
  const [openPicker, setOpenPicker] = useState(false);
  const location = useLocation();
  const [expenseIncome, setExpenseIncome] = useState(null);
  const [displayFilters, setDisplayFilters] = useState(false);
  const [toggledArrow, setToggledArrow] = useState(false);
  const [displaySort, setDisplaySort] = useState(false);
  const [activeModal, setActiveModal] = useState({ modal: null });
  const loadMoreRef = useRef(null);
  const [appliedFilters, setAppliedFilters] = useState({
    month: "",
    category: "",
    amountMin: "",
    amountMax: "",
    sort: "",
    search: ""
  });

  const sortOptions = [
    { label: "category" },
    { label: "date" },
    { label: "amount" },
    { label: "payment" },
  ]

  const [transactionFilters, setTransactionFilters] = useState({
    month: "",
    category: "",
    amountMin: "",
    amountMax: "",
    sort: "",
    search: ""
  });


  const fetchExpenses = async (pageParam) => {

    const res = await api.get("/expenses/get-expenses",
      {
        params: {
          page: pageParam,
          size: 5,
          sort: appliedFilters.sort,
          month: appliedFilters.month,
          category: appliedFilters.category,
          amountMin: appliedFilters.amountMin,
          amountMax: appliedFilters.amountMax,
          search: appliedFilters.search
        }
      }
    );
    console.log("expenses", res.data);

    return res.data;
  }

  const { data: expenses = { pages: [], pageParams: [] }, isFetchingNextPage, isLoading, isFetching, hasNextPage, fetchNextPage } = useInfiniteQuery({
    queryKey: ["expenses", appliedFilters],
    queryFn: ({ pageParam }) => fetchExpenses(pageParam),
    initialPageParam: 0,
    getNextPageParam: (lastPage) => {
      if (lastPage.last) {
        return undefined;
      }
      return lastPage.number + 1;
    }
  })


  useEffect(() => {
    if (!loadMoreRef.current) return;
    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && !isFetchingNextPage && hasNextPage) {
        fetchNextPage();
      }
    });

    if (loadMoreRef.current !== null) {
      observer.observe(loadMoreRef.current);
    }

    return () => {
      observer.disconnect();
    }
  }, [isFetchingNextPage, hasNextPage, fetchNextPage]);


  const getElement = (elementId) => {
    let input;
    let component;

    const choices = [
      { id: "1", component: MonthListFilter },
      { id: "2", component: CategoryListFilter },
      { id: "3", component: AmountListFilter }
    ];

    return function innerFun() {
      input = choices.find((choice) => choice.id === elementId);
      component = input.component;
      return component;
    }
  }

  const btns = [
    { category: "Income", bg: "bg-white/10" },
    { category: "Expenses", bg: "bg-white/10" }
  ];


  const handleModalPick = (e) => {
    const element = e.target.closest("[data-id]");
    if (!element) return;
    const getEl = getElement(element.dataset.id);
    const component = getEl();
    setActiveModal(prev => ({ ...prev, modal: component }));
    setOpenPicker(true);
  }

  const sortFields = (
    <div className='flex gap-2 overflow-x-auto no-scrollbar w-full px-6 py-3 bg-white/5 backdrop-blur-md border-y border-white/10 mt-2'>
      {sortOptions.map((option, idx) => (
        <button
          key={idx}
          className={`shrink-0 flex items-center  gap-3 justify-center px-5 py-2 rounded-xl font-bold text-sm transition-all ${transactionFilters.sort === option.label
            ? "bg-blue-500/30 text-white"
            : "bg-white/10 text-white/80 hover:bg-white/20"
            }`}
          onClick={() => {
            setTransactionFilters(prev => (
              { ...prev, sort: option.label + "," + (toggledArrow ? "desc" : "asc") }
            ));
            setToggledArrow(!toggledArrow);
          }}
        >
          {option.label}
          {(toggledArrow && transactionFilters.sort.split(",")[0] === option.label)
            ? <LuArrowUp className='relative top-[1px]' />
            : <LuArrowDown className='relative top-[1px]' />
          }
        </button>
      ))}
    </div>
  );
  const filters = (
    (
      <div
        onClick={handleModalPick}
        className="flex justify-center gap-1 overflow-x-auto no-scrollbar w-full py-2">
        <MonthFilter className="shrink-0" transactionFilters={transactionFilters} setTransactionFilters={setTransactionFilters} />
        <CategoryFilter className="shrink-0" transactionFilters={transactionFilters} setTransactionFilters={setTransactionFilters} />
        <AmountFilter className="shrink-0" transactionFilters={transactionFilters} setTransactionFilters={setTransactionFilters} />
      </div>
    )
  );

  const renderExpensesList = () => {
    if (isLoading) return <div className="flex items-center justify-center h-screen">
      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-blue-500"></div>
    </div>;
    return expenses.pages.flatMap(page => page.content).map((expense, idx) => {
      console.log("expense", expense);


      let categoryRow = categories.find(category => category.id === expense.categoryId);
      return <Expense
        key={expense.id}
        categoryRow={categoryRow}
        expense={expense}
      />
    })
  }


  return (
    <div className='h-screen w-full overflow-hidden flex flex-col lg:flex-row bg-slate-950'>
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
       <main className='flex-1 flex flex-col h-full overflow-hidden relative bg-gradient-to-b from-slate-950 via-slate-900 to-slate-950'>
        <section className='transactions-top-part flex flex-col items-center pt-2 bg-[linear-gradient(to_bottom,rgba(0,0,0,0.6),rgba(0,0,0,0.2)),url("/login-page.jpg")] bg-cover bg-center shrink-0 min-h-[250px] lg:min-h-[300px]'>
          <h1 className='text-white font-bold text-xl tracking-widest'>{t("transactions")}</h1>

          <div className='transactions-header-content flex flex-col relative flex-1 items-center w-full max-w-4xl mx-auto'>

            <div className='search-input flex items-center w-full px-6 mb-2'>
              <input
                onChange={(e) => setTransactionFilters(prev => ({
                  ...prev,
                  search: e.target.value
                }))}
                value={transactionFilters.search}
                className='bg-white/10 backdrop-blur-md border border-white/20 px-4 py-2.5 rounded-2xl w-full text-white placeholder-white/50 outline-none focus:ring-2 focus:ring-blue-400/50 transition-all'
                type="text"
                placeholder='Search transactions...'
              />
            </div>

            <div className="mt-4 w-[85%] lg:w-full lg:px-6 flex gap-3">
              <button
                onClick={() => {
                  setDisplaySort(!displaySort);
                  setDisplayFilters(false);
                }}
                className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-2xl font-bold transition-all border ${displaySort ? "bg-white/80 text-blue-600 border-white" : "bg-white/10 text-white border-white"}`}
              >
                <LuChevronsUpDown size={18} /> {t("sort")}
              </button>

              <button
                onClick={() => { setDisplayFilters(!displayFilters); setDisplaySort(false); }}
                className={`flex-1 flex items-center justify-center gap-2 py-3 rounded-2xl font-bold transition-all border ${displayFilters ? "bg-white/80 text-blue-600 border-white" : "bg-white/10 text-white border-white"}`}
              >
                <LuChevronDown size={18} /> {t("filter")}
              </button>
            </div>
            <div className="w-full relative px-6">
              {displaySort && <div className="absolute top-2 left-6 right-6 z-50">{sortFields}</div>}
              {displayFilters && <div className="absolute top-2 left-6 right-6 z-50">{filters}</div>}
            </div>
            {openPicker && (
              <div className="fixed inset-0 z-[100] flex items-center justify-center bg-white/10  p-6">
                <activeModal.modal
                  setOpenPicker={setOpenPicker}
                  setTransactionFilters={setTransactionFilters}
                />
              </div>
            )}
            <div className='absolute bottom-2 left-1/2 -translate-x-1/2 z-20'>
              <button
                onClick={() => setAppliedFilters(prev => ({
                  ...prev,
                  month: transactionFilters.month,
                  category: transactionFilters.category.name,
                  amountMin: transactionFilters.amountMin,
                  amountMax: transactionFilters.amountMax,
                  sort: transactionFilters.sort,
                  search: transactionFilters.search
                }))}
                className='bg-red-500/40 backdrop-blur-xl px-6 py-2 rounded-xl text-white font-bold border border-white/20 shadow-lg active:scale-95 transition-all'
              >
                {t("apply")}
              </button>
            </div>
          </div>
        </section>
        <section className="expenses-container flex flex-col flex-1 w-full gap-4 overflow-y-auto bg-slate-50 -mt-6 pt-10 rounded-t-[40px] z-10 shadow-[0_-10px_20px_rgba(0,0,0,0.05)]">
          <div className="max-w-4xl mx-auto w-full px-4">
            {renderExpensesList()}

            <div className='h-[10vh] mb-10 mx-auto animate-pulse rounded-lg bg-gray-200/40 flex items-center justify-center mt-10'>
              <span ref={loadMoreRef} className={`text-gray-400 text-[14px] uppercase ${isLoading ? "hidden" : "block "}`}>
                {hasNextPage ? "Loading more" : "No more items"}
              </span>
            </div>
          </div>
        </section>
      </main>
      {openPicker && (
        <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/40 backdrop-blur-sm p-6">
          <activeModal.modal setOpenPicker={setOpenPicker} setTransactionFilters={setTransactionFilters} />
        </div>
      )}

      <Outlet categories={categories} />

      <div className="lg:hidden shrink-0">
        <Navbar />
      </div>
    </div>
  )
}

export default Transactions