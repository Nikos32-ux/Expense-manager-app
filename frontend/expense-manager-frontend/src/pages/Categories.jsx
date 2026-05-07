import React, { useMemo, useState } from 'react';
import { useTranslation } from 'react-i18next';
import { Link } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import { Chart, ArcElement, Tooltip, Legend, plugins } from 'chart.js';
import { Doughnut } from 'react-chartjs-2';
import { LuUtensils, LuHeartPulse, LuZap, LuPlane, LuTv, LuShoppingBag,LuPanelTop,LuLayoutGrid,LuWallet,LuUser,LuLogOut, LuCar, LuBookOpen, LuChevronRight } from 'react-icons/lu';
import Navbar from '../components/ui/Navbar';
import { categories } from '../categories/categories';
import api from '../axiosClientApi/axios';
import LoadSpinner from '../components/ui/LoadSpinner';
import useCategories from '../hooks/useCategories';

Chart.register(ArcElement, Tooltip, Legend);

const Categories = () => {
  const { t } = useTranslation();
  const [chosenFilter, setChosenFilter] = useState(null);
  const { categoriesTotal, totalIsLoading } = useCategories({ chosenFilter });

  const filters = ["day", "week", "month"];

  const categoriesMap = useMemo(() => {
    const map = new Map();
    categories.forEach(category => map.set(category.name, category));
    return map;
  }, []);

  const categoriesTop = useMemo(() => {
    if (!categoriesTotal) return [];
    return categoriesTotal
      .sort((a, b) => b.total - a.total)
      .slice(0, 3)
      .map(cat => ({ category: cat.category, total: cat.total, hex: categoriesMap.get(cat.category).hex }));
  }, [categoriesTotal]);



  const totalSpent = useMemo(() => {
    if (!categoriesTotal) return;
    const totalSum = categoriesTotal.reduce((a, b) => a + b.total, 0);
    return totalSum;
  }, [categoriesTotal]);

  const data = {
    labels: categoriesTop.map(cat => cat.category),
    datasets: [
      {
        data: categoriesTop.map(cat => cat.total),
        backgroundColor: categoriesTop.map(cat => cat.hex),
        hoverOffset: 10,
        borderWidth: 0
      },
    ]
  };


  const options = {
    cutout: '75%',
    responsive: true,
    maintainAspectRatio: true,

    plugins: {
      legend: {
        display: false,
        labels: {
          usePointStyle: true,
          pointStyle: 'circle',
          padding: 12
        }
      }
    }
  };


  const noTotal =
    <div className="flex bg-gray-200/10  flex-col items-center justify-center mt-20 opacity-60">
      <div className="bg-gray-200 p-6 rounded-full mb-4">
        <LuShoppingBag size={48} className="text-gray-400" />
      </div>
      <h3 className="text-gray-600 font-bold text-lg">{t("zero-transactions")}</h3>
      <p className="text-gray-500 text-sm text-center px-10">
        {t("choose-filter-add-expense")}
      </p>
    </div>

  const categoryTotal =
    categoriesTotal.map((category, i) => {
      const Icon = categoriesMap.get(category.category).icon;
      const bg = categoriesMap.get(category.category).bg;
      const text = categoriesMap.get(category.category).text;
      return (
        <div key={i} className="bg-white/80 p-1 rounded-2xl shadow-md border border-gray-100 flex justify-between items-center">
          <div className='flex items-center gap-2'>
            <div className={`w-10 h-10 ${bg} rounded-xl flex items-center justify-center`}>
              <Icon size={20} className={text} />
            </div>

            <span className="font-bold text-gray-700 text-xl">{t(category.category)}</span>
          </div>
          <div className='flex gap-3 px-3'>
            <span className="font-bold text-gray-900 text-lg tracking-tight tabular-nums">${category.total || 0} </span>

          </div>
        </div>
      )
    })


  return (
    <div className='h-[100dvh] w-full flex flex-col bg-slate-50 lg:flex-row'>
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
      <div className="flex flex-1 flex-col h-full overflow-hidden relative">
        <div className="header flex flex-col bg-[linear-gradient(to_bottom,rgba(0,0,0,0.7),rgba(0,0,0,0.3)),url('/login-page.jpg')] bg-cover bg-center h-[28vh] shrink-0">
          <h1 className='text-white text-center mt-4 font-bold text-2xl tracking-widest uppercase'>{t("categories-header")}</h1>
          <div
            onClick={(e) => {
              const element = e.target.closest("[data-id]");
              if (element) setChosenFilter(filters[element.dataset.id]);
            }}
            className="buttons flex items-center backdrop-blur-sm mt-6 justify-center gap-1 px-2">
            {filters.map((filter, i) => {
              return (
                <button
                  data-id={i}
                  key={i}
                  className={`flex-1 ${chosenFilter === filter ? "bg-blue-600" : "bg-white/10"}  text-white py-2 rounded-lg shadow-lg`}>
                  {t(filters[i])}
                </button>
              )
            })}
          </div>
          <div className=' flex flex-col items-center mt-4'>
            <input
              type="date"
              id='choose-date'
              className='opacity-0 absolute cursor-pointer '
            />
          </div>
        </div>
        <div className="chart-container relative -mt-3 mx-auto w-[90%] bg-white/20 backdrop-blur-lg rounded-[30px] shadow-2xl p-2 flex justify-center">
          <div className="flex justify-between items-center gap-7">
            <div className='relative h-32 w-32'>
              <Doughnut data={data} options={options} />
              <div className="absolute inset-0 flex flex-col items-center justify-center pointer-events-none">
                <p className="text-gray-400 text-[10px] uppercase font-semibold">{t("total-spent")}</p>
                <p className="text-[20px] font-bold text-gray-800">${totalSpent}</p>
              </div>
            </div>

            <div className="flex flex-col gap-3">
              {data.labels.map((label, i) => (
                <div key={i} className="flex items-center gap-2">
                  <span
                    className="w-3 h-3 rounded-full"
                    style={{ backgroundColor: data.datasets[0].backgroundColor[i] }}
                  >
                  </span>
                  <span className="text-gray-700 font-semibold">{t(label)}</span>
                </div>
              ))}
            </div>
          </div>
        </div>
        <div className="categories-list flex flex-col flex-1 overflow-y-auto px-5 pb-10 pt-10 gap-2">
          {totalIsLoading ? <LoadSpinner /> : categoriesTotal.length > 0 ? categoryTotal : noTotal}
        </div>
        <div className="lg:hidden shrink-0">
          <Navbar />
        </div>
      </div>
    </div>
  )
}

export default Categories