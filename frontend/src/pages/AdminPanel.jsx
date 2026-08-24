import React from 'react';
import { LuLogOut } from 'react-icons/lu';
import { Link, Outlet, NavLink, Navigate, useOutletContext } from 'react-router-dom';

function AdminPanel() {
  const { user, isLoading } = useOutletContext();
  if (user.role !== "ROLE_ADMIN") return <Navigate to="/dashboard" replace />


  return (

    <div className="flex flex-col lg:flex-row lg:h-screen h-screen bg-slate-50 font-sans text-slate-900 pb-20 lg:pb-0">
      <aside className="hidden lg:flex lg:w-72 lg:h-full lg:flex-col bg-slate-950 text-white shrink-0 border-r border-white/5 relative overflow-hidden 
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
            <div className=' flex lg: mt-5 flex-col gap-5 py-5 w-full items-start'>
              <NavLink
                to=""
                end
                className={({ isActive }) => `flex flex-col lg:flex-row lg:gap-3 items-center justify-center flex-1 h-full font-bold text-xs gap-1 w-full p-2.5 ${isActive ? "bg-blue-600 text-white rounded-xl" : "text-slate-400 hover:text-white"}`}
              >
                <span className="text-base">🏠</span>
                <span>Overview</span>
              </NavLink>

              <NavLink
                to="users"
                end
                className={({ isActive }) => `flex flex-col lg:flex-row lg:gap-3 items-center justify-center flex-1 h-full font-bold text-xs gap-1 w-full p-2.5 ${isActive ? "bg-blue-600 text-white rounded-xl" : "text-slate-400 hover:text-white"}`}
              >
                <span className="text-base">👥</span>
                <span>Users</span>
              </NavLink>
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

      <Outlet />

      <nav className="fixed lg:hidden lg:z-0 bottom-0 left-0 right-0 h-[70px] bg-slate-800 border-t border-slate-700 flex items-center justify-around z-60">
        <NavLink to="" end className={({ isActive }) => `flex flex-col items-center justify-center flex-1 h-full text-white font-bold text-xs gap-1 ${isActive ? "bg-blue-600 text-white rounded-xl" : "text-slate-400 hover:text-white"}`}>
          <span className="text-base">🏠</span>
          <span>Overview</span>
        </NavLink>
        <NavLink to="users" end className={({ isActive }) => `flex flex-col items-center justify-center flex-1 h-full text-white font-bold text-xs gap-1 ${isActive ? "bg-blue-600 text-white rounded-xl" : "text-slate-400 hover:text-white"}`}>
          <span className="text-base">👥</span>
          <span>Users</span>
        </NavLink>
      </nav>
    </div>

  )
}

export default AdminPanel