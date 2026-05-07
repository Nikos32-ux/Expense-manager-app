import { useContext, useEffect, useState } from 'react';
import { WebSocketContext } from '../context/WebsocketContext.jsx';
import toast, { Toaster } from 'react-hot-toast';
import { Link, Outlet, useRouteLoaderData } from 'react-router-dom';
import { LuUser, LuBell, LuChevronLeft, LuSettings2, LuDownload, LuPanelTop, LuShieldCheck, LuLayoutGrid, LuWallet, LuFileUp, LuLogOut, LuFileText } from 'react-icons/lu';
import Navbar from '../components/ui/Navbar.jsx';
import { useMutation, useQuery } from '@tanstack/react-query';
import { verifyUser } from '../queries/authQuery.js'
import { useTranslation } from 'react-i18next';
import api from '../axiosClientApi/axios.js';
import NotificationPopUp from '../components/profile-modals/NotificationPopUp.jsx';
import { formatDistanceToNow } from "date-fns";
import { queryClient } from '../context/queryClient.js';
import useProfile from '../hooks/useProfile.jsx';


const Profile = () => {
  const { notifications } = useContext(WebSocketContext);
  const user = useRouteLoaderData("root");
  const { t, i18n } = useTranslation();
  const [openNotificationById, setOpenNotificationById] = useState(null);
  const [openDropdown, setOpenDropDown] = useState(false);

  const { exportFileMutate, exportFileisPending } = useProfile({ setOpenNotificationById });

  const handleBell = async () => {
    setOpenDropDown(prev => !prev);
  }


  const openNotification = async (id) => {
    try {
      const notificationOpened = notifications.find(n => n.id === id);
      if (notificationOpened && notificationOpened.isRead === true) {
        console.log("already marked read, no api call");
        return;
      }
      const res = await api.put(`notifications/mark-as-read/${id}`);
      queryClient.setQueryData(
        ["notifications"],
        (old = []) => old.map(n => n.id === id ? { ...n, isRead: true } : n)
      )

    } catch (error) {
      console.error("Error: ", error?.response?.message)
    }
  }

  return (
    <div className='h-[100dvh] w-full flex flex-col overflow-hidden lg:flex-row'>
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
      <div className='flex-1 flex flex-col'>
        <div className="header w-full bg-[linear-gradient(to_bottom,rgba(0,0,0,0.3),rgba(0,0,0,0.4)),url('/login-page.jpg')] h-[30vh] rounded-b-3xl p-3 z-10">
          <div className='header-navigation relative flex w-full justify-between items-center mt-2 px-2'>
            <Link to={"/dashboard"} className='bg-white/10 p-2 hover:bg-white/20 transition-colors'>
              <LuChevronLeft size={24} className="text-white" />
            </Link>
            <div>
              <p className='font-semibold text-white tracking-widest text-xl'>{t("profile")}</p>
            </div>
            <div className='relative'>
              <button onClick={handleBell} className="bg-white/10 p-2 hover:bg-white/20 transition-colors">
                <LuBell size={24} className='text-white' />
                {notifications.filter(n => !n.isRead).length > 0 && (
                  <span className="w-2 h-2 bg-red-600 rounded-full absolute -top-1 right-0" />
                )}
              </button>
              {openDropdown && (
                <div className="absolute right-0 mt-3 w-72 bg-white rounded-xl shadow-2xl py-2 z-50 border border-gray-100 animate-in fade-in zoom-in duration-200">
                  <div className="px-4 py-2 border-b border-gray-100">
                    <p className="font-bold text-gray-800 text-sm">{t("notifications")}</p>
                  </div>
                  <div className="max-h-64 overflow-y-auto">
                    {notifications.map((notification, i) => {
                      return (
                        <div
                          key={notification.id}
                          onClick={() => {
                            openNotification(notification.id);
                            setOpenNotificationById(notification.id);
                          }}
                          className="flex items-center gap-3 px-4 py-3 hover:bg-gray-50 cursor-pointer transition-colors"
                        >
                          <div className="bg-blue-100 p-2 rounded-full">
                            <LuFileText className="text-blue-600" size={18} />
                          </div>
                          <div className="flex-1">
                            <p className={`text-sm ${notification.isRead ? "" : "font-bold"} text-gray-900`}>{t("FILE_GENERATED")}</p>
                            <p className="text-[10px] text-gray-400 font-medium uppercase tracking-wider mt-1">
                              {formatDistanceToNow(notification.sentAt, { addSuffix: true })}
                            </p>
                          </div>
                        </div>
                      )
                    })}
                  </div>
                  {notifications.length === 0 && (
                    <p className="text-center py-4 text-xs text-gray-400">{t("no-new-alerts")}</p>
                  )}
                </div>
              )}
              {openNotificationById && 
                <NotificationPopUp 
                  setOpenDropDown={setOpenDropDown} 
                  setOpenNotificationById={setOpenNotificationById} 
                  openNotificationById={openNotificationById} 
                />
              }
            </div>
          </div>
          <div className="profile-pic w-24 h-24 rounded-full border-2 border-white mt-4 mx-auto">
            <img src={user.imageProfile} className='w-full h-full object-cover rounded-full' alt="" />
          </div>
        </div>
        <div className="settings-container bg-gray-50 flex flex-col h-full p-4 flex-1 overflow-auto-y -mt-4">
          <div className='pt-8 w-full mt-3'>
            <div className='mx-auto text-center text-gray-900'>
              <p className='text-3xl font-bold italic'>{user.username}</p>
              <p className='text-lg text-blue-400'>{user.email}</p>
            </div>
          </div>
          <div className="settings-list w-[90%]  mx-auto py-5 text-center p-4 flex flex-col gap-5">
            <Link
              to={"account-info"}
              className=' flex items-center gap-4 p-4  bg-white shadow hover:shadow-lg transition-shadow duration-200 rounded-sm'
            >
              <LuUser size={24} className='text-blue-600' />
              <p className='text-lg font-medium text-gray-800'>{t("account-info")}</p>
            </Link>
            <Link
              to={"security"}
              className='flex items-center bg-white gap-4 p-4 shadow hover:shadow-lg transition-shadow duration-200 rounded-sm'>
              <LuShieldCheck size={24} className='text-blue-600' />
              <p className='text-lg font-medium text-gray-800'>{t("security")}</p>
            </Link>
            <button onClick={() => exportFileMutate()} className='flex items-center bg-white gap-4 p-4 shadow hover:shadow-lg transition-shadow duration-200 rounded-sm'>
              <LuFileUp size={24} className='text-blue-600' />
              <span className='text-lg font-medium text-gray-800'>
                {t("data-export")}
              </span>
            </button>
            <div className='flex items-center  gap-4 p-4 shadow hover:shadow-lg transition-shadow duration-200 rounded-sm'>
              <LuLogOut size={24} className='text-red-600' />
              <p className='text-lg font-medium text-red-700'>{t("logout")}</p>
            </div>
          </div>
        </div>
      </div>
      <div className="lg:hidden shrink-0">
        <Navbar />
      </div>
      <Outlet />
    </div>
  )
}

export default Profile