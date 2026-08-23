import React, { useEffect, useRef, useState } from 'react';
import useAdminUsers from '../../hooks/useAdminUsers';
import LoadSpinner from '../ui/LoadSpinner';
import { useTranslation } from 'react-i18next';

const AdminUsers = () => {
  const loadMoreRef = useRef(null);
  const [draftInput, setDraftInput] = useState("");
  const [searchInput, setSearchInput] = useState("");

  const { t } = useTranslation();

  const {
    adminUsersData,
    adminUsersDataIsFetchingNextPage,
    adminUsersDataLoading,
    hasNextPage,
    fetchNextPage,
    adminSearchUserDataSuccess,
    adminSearchUserData,
    adminSearchUserDataLoading
  } = useAdminUsers({searchInput});

  useEffect(() => {
    if (!loadMoreRef.current) return;

    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && !adminUsersDataIsFetchingNextPage && hasNextPage) {
        fetchNextPage();
      }
    }, {
      rootMargin: '0px 0px -100px 0px'
    });

    if (loadMoreRef.current !== null) {
      observer.observe(loadMoreRef.current);
    }

    return () => {
      observer.disconnect();
    }
  }, [adminUsersDataIsFetchingNextPage, hasNextPage, fetchNextPage]);


  const renderUsers = () => {
    console.log("renderusers triggered", adminUsersData?.pages);
    
    return (
      adminUsersData?.pages?.flatMap(page => page.content).map((userDataEntry, idx) => {
        return (
          <div key={idx} className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <span className="text-xs font-bold text-slate-500">{userDataEntry.id}</span>
            </div>
            <div>
              <p className="text-sm font-semibold text-slate-900">{userDataEntry.email}</p>
              <p className="text-xs text-slate-500">Registered: <span>{userDataEntry.timestamp}</span></p>
            </div>
            <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
              <span className="text-indigo-600 font-semibold cursor-pointer">Edit</span>
              <span className="text-red-500 font-semibold cursor-pointer">Delete</span>
            </div>
          </div>
        )
      })
    )
  }

  const renderUser = () => {
    console.log("renderUser triggered");
    return (
      <div className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm flex flex-col gap-3">
        <div className="flex items-center justify-between">
          <span className="text-xs font-bold text-slate-500">{adminSearchUserData?.id}</span>
        </div>
        <div>
          <p className="text-sm font-semibold text-slate-900">{adminSearchUserData?.email}</p>
          <p className="text-xs text-slate-500">Registered: <span>{adminSearchUserData?.timestamp}</span></p>
        </div>
        <div className="flex items-center justify-between pt-2 border-t border-slate-100 text-xs">
          <span className="text-indigo-600 font-semibold cursor-pointer">Edit</span>
          <span className="text-red-500 font-semibold cursor-pointer">Delete</span>
        </div>
      </div>
    )
  }

  return (
    <div className="flex flex-col flex-1 h-full bg-slate-50 font-sans text-slate-900 overflow-y-auto">

      <header className="h-[60px] bg-white border-b border-slate-200 flex items-center justify-between px-4 sticky top-0 z-10">
        <h2 className="text-[16px] font-bold text-slate-900">User Management</h2>
        <div className="w-7 h-7 rounded-full bg-slate-300 flex items-center justify-center text-xs font-bold text-slate-700">A</div>
      </header>

      <main className="p-4 flex flex-col gap-4">
        <div className="searchInput flex items-center justify-between gap-2 bg-white p-3 rounded-lg border border-slate-200 shadow-sm">
          <input
            onChange={(e) => {
              if(e.target.value.length === 0) {
                setSearchInput("");
              }
              setDraftInput(e.target.value)
            }}
            onKeyDown={(e) => {
              if (e.key === "Enter") {
                setSearchInput(draftInput);
              }
            }}
            type="text"
            placeholder="Search by email..."
            className="w-full bg-slate-100 px-3 py-2 rounded-md text-xs text-slate-800 outline-none border border-slate-200 focus:border-blue-500"
          />
          <button onClick={() => {
            if(draftInput.length === 0) return;
            setSearchInput(draftInput);
          }} className="bg-indigo-600 hover:bg-indigo-700 text-white text-xs font-bold px-3 py-2 rounded-md whitespace-nowrap transition-colors">
            Search
          </button>
        </div>
          
        <div className="usersList flex flex-col gap-3 pb-20">
          {
            adminUsersDataLoading
              ? <LoadSpinner />
                : searchInput
                ? renderUser()
              : renderUsers()
          }

          <div className='h-20 flex items-center justify-center mt-5'>
            <span ref={loadMoreRef} className={`text-gray-400 text-[14px] uppercase`}>
              {adminUsersDataIsFetchingNextPage ? (
                <LoadSpinner size="small" />
              ) : !hasNextPage ? (
                t("no-more-users")
              ) : null}
            </span>
          </div>
        </div>
      </main>
    </div>
  );
};

export default AdminUsers;