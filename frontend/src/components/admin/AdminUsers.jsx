import React from 'react';

const AdminUsers = () => {
  return (
    <div className="flex flex-col flex-1 h-full bg-slate-50 font-sans text-slate-900 overflow-y-auto">
      
      <header className="h-[60px] bg-white border-b border-slate-200 flex items-center justify-between px-4 sticky top-0 z-10">
        <h2 className="text-[16px] font-bold text-slate-900">User Management</h2>
        <div className="w-7 h-7 rounded-full bg-slate-300 flex items-center justify-center text-xs font-bold text-slate-700">A</div>
      </header>
      
      <main className="p-4 flex flex-col gap-4">
        <div className="searchInput flex items-center justify-between gap-2 bg-white p-3 rounded-lg border border-slate-200 shadow-sm">
          <input 
            type="text" 
            placeholder="Search by email..." 
            className="w-full bg-slate-100 px-3 py-2 rounded-md text-xs text-slate-800 outline-none border border-slate-200 focus:border-blue-500"
          />
          <button className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold px-3 py-2 rounded-md whitespace-nowrap transition-colors">
            Search
          </button>
        </div>

        <div className="usersList flex flex-col gap-3">

          <div className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500">#1001</span>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700">ACTIVE</span>
            </div>
            <div>
              <p className="text-sm font-semibold text-slate-900">nikos@example.com</p>
              <p className="text-xs text-slate-500">Registered: 2023-10-25</p>
            </div>
            <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
              <span className="text-indigo-600 font-semibold cursor-pointer">Edit</span>
              <span className="text-red-500 font-semibold cursor-pointer">Delete</span>
            </div>
          </div>

          <div className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500">#1002</span>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700">ACTIVE</span>
            </div>
            <div>
              <p className="text-sm font-semibold text-slate-900">maria@example.com</p>
              <p className="text-xs text-slate-500">Registered: 2023-10-26</p>
            </div>
            <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
              <span className="text-indigo-600 font-semibold cursor-pointer">Edit</span>
              <span className="text-red-500 font-semibold cursor-pointer">Delete</span>
            </div>
          </div>

          <div className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500">#1003</span>
              <span className="text-[10px] font-bold px-2 py-0.5 rounded-full bg-amber-100 text-amber-700">UNVERIFIED</span>
            </div>
            <div>
              <p className="text-sm font-semibold text-slate-900">newuser@example.com</p>
              <p className="text-xs text-slate-500">Registered: 2023-11-05</p>
            </div>
            <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
              <span className="text-indigo-600 font-semibold cursor-pointer">Edit</span>
              <span className="text-red-500 font-semibold cursor-pointer">Delete</span>
            </div>
          </div>

        </div>
      </main>
    </div>
  );
};

export default AdminUsers;