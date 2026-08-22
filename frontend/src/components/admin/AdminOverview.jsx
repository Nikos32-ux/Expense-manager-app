import React, { useActionState, useEffect, useRef } from 'react';
import { LuLogOut } from 'react-icons/lu';
import { Link, Outlet } from 'react-router-dom';
import useAdminOverview from '../../hooks/useAdminOverview.jsx';
import LoadSpinner from '../ui/LoadSpinner';
import { useInfiniteQuery } from '@tanstack/react-query';
import { useTranslation } from 'react-i18next';
import { formatDistanceToNow } from "date-fns";



const AdminOverview = () => {
    const { t } = useTranslation();
  
  const loadMoreRef = useRef(null);

  const {
    overviewData,
    auditLogsData,
    overviewDataLoading,
    auditLogsDataLoading,
    userActionLogs,
    fetchNextPage,
    isError,
    isFetching,
    isFetchingNextPage,
    hasNextPage,
    userActionLogsLoading
  } = useAdminOverview();


  useEffect(() => {
    if (!loadMoreRef.current) return;

    const observer = new IntersectionObserver((entries) => {
      if (entries[0].isIntersecting && !isFetchingNextPage && hasNextPage) {
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
  }, [isFetchingNextPage, hasNextPage, fetchNextPage]);

  return (

    <div className="flex lg:flex-1 lg:h-full flex-col h-full overflow-y-auto bg-slate-50 font-sans text-slate-900">
      <header className="h-[60px] bg-white border-b border-slate-200 flex items-center justify-between px-4 sticky top-0 z-10">
        <h2 className="text-[16px] font-bold text-slate-900">Overview Dashboard</h2>
        <div className="w-7 h-7 rounded-full bg-slate-300"></div>
      </header>

      <main className="p-4 flex flex-col gap-4">

        <div className='flex items-center justify-around gap-4'>
          <div className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm flex-1">
            <p className="text-xs text-slate-500 font-medium mb-1">Total Users</p>
            <div className="text-xl font-bold text-slate-900">{overviewDataLoading ? <LoadSpinner /> : overviewData?.totalUsers}</div>
          </div>

          <div className="bg-white p-4 rounded-lg border border-slate-200 shadow-sm flex-1">
            <p className="text-xs text-slate-500 font-medium mb-1">Total Expenses</p>
            <div className="text-xl font-bold text-slate-900">{overviewDataLoading ? <LoadSpinner /> : overviewData?.totalExpenses}</div>
          </div>
        </div>

        <section className="bg-white rounded-lg border border-slate-200 shadow-sm p-4 mt-2">
          <h3 className="text-sm font-bold text-slate-900 mb-3">Recent Activity Stream</h3>


          <div className="flex flex-col gap-2.5 max-h-[350px] overflow-y-auto [scrollbar-width:none] [&::-webkit-scrollbar]:hidden pb-20">
            {
              userActionLogsLoading
                ? <LoadSpinner />
                : (
                  userActionLogs.pages.flatMap(page => page.content).map((userActionLog, idx) => {
                    return <div key={idx} className="p-2.5 rounded-md bg-slate-50 flex flex-col gap-0.5">
                      <span className="text-xs font-semibold text-slate-800">{userActionLog?.action}</span>
                      <span className="text-xs text-slate-600">{userActionLog?.email} <span>{formatDistanceToNow(userActionLog?.timestamp, { addSuffix: true })}</span></span>
                    </div>
                  })
                )
            }

            <div className='h-20 flex items-center justify-center mt-5'>
              <span ref={loadMoreRef} className={`text-gray-400 text-[14px] uppercase`}>
                {isFetchingNextPage ? (
                  <LoadSpinner size="small" />
                ) : !hasNextPage ? (
                  t("no-more-logs")
                ) : null}
              </span>
            </div>

          </div>
        </section>

      </main>
    </div>
  )
}

export default AdminOverview;