import React from 'react'
import { useTranslation } from 'react-i18next';
import { LuWallet, LuUser, LuLayoutGrid, LuLayoutDashboard, LuPanelTop } from 'react-icons/lu';
import { Link } from 'react-router-dom';
import { queryClient } from '../../context/queryClient';

const Navbar = () => {
    const { t, i18n } = useTranslation();
    const user = queryClient.getQueryData(["verification"]);
    return (

        <div className='bg-white/90 h-14 backdrop-blur-md flex w-full justify-around items-center shadow-md pt-2 pb-2'>
            <div className='flex flex-col justify-center items-center'>
                <Link to={"/dashboard"}><LuPanelTop size={24} className='text-gray-600 hover:text-blue-500' /></Link>
                <p className='text-sm text-gray-800/80'>{t("dashboard")}</p>
            </div>
            <div className='flex flex-col justify-center items-center'>
                <Link to={"/transactions"}><LuWallet size={24} className='text-gray-600 hover:text-blue-500' /></Link>
                <p className='text-sm text-gray-800/80'>{t("expenses")}</p>
            </div>
            <div className='flex flex-col justify-center items-center'>
                <Link to={"/categories"}><LuLayoutGrid size={24} className='text-gray-600 hover:text-blue-500' /></Link>
                <p className='text-sm text-gray-800/80'>{t("categories")}</p>
            </div>
            <div className='flex flex-col justify-center items-center'>
                <Link to={"/profile"}><LuUser size={24} className='text-gray-600 hover:text-blue-500' /></Link>
                <p className='text-sm text-gray-800/80'>{t("profile")}</p>
            </div>
            {
                user?.role === "ROLE_ADMIN" &&
                <div className='flex flex-col justify-center items-center'>
                    <Link to={"/admin"}><LuUser size={24} className='text-gray-600 hover:text-blue-500' /></Link>
                    <p className='text-sm text-gray-800/80'>{t("admin")}</p>
                </div>
            }

        </div>
    )
}

export default Navbar