import React, { useContext, useEffect, useState } from 'react'
import { LuUser, LuBell, LuChevronLeft, LuSettings2, LuDownload, LuShieldCheck, LuFileUp, LuLogOut, LuActivity } from 'react-icons/lu';
import { WebSocketContext } from '../../context/WebsocketContext';
import { formatDistanceToNow } from "date-fns";


const NotificationPopUp = ({ setOpenDropDown, setOpenNotificationById, openNotificationById }) => {
    const { notifications } = useContext(WebSocketContext);


    const notificationTypes = [
        {
            id: 1,
            type: "FILE_GENERATED",
            status: "Report ready",
            desc: " Your monthly expense summary has been successfully generated. This report includes all transactions,you can download it below.",
            icon: <LuFileUp/>
        }
    ];

    const activeNotification = notifications.find(n => n.id === openNotificationById);
    const activeNotificationData = notificationTypes.find(notType => notType.type === activeNotification?.type);



    return (
        <div className="fixed inset-0 bg-black/50 backdrop-blur-sm z-[100] flex items-center justify-center p-4">
            <div className="bg-white w-full  rounded-2xl shadow-2xl overflow-hidden">

                <div className="p-4 border-b border-gray-100 flex justify-between items-center">
                    <h2 className="text-lg font-bold text-gray-800">Notification Detail</h2>
                    <button onClick={() => setOpenNotificationById(null)} className="text-gray-400 hover:text-gray-600 text-2xl">&times;</button>
                </div>

                <div className="p-6">
                    <div className="flex items-center gap-3 mb-4">
                        <span className="p-2 bg-blue-100 text-blue-500 rounded-full">
                            {activeNotificationData.icon};
                        </span>
                        <div>
                            <p className="text-xs text-gray-400">{formatDistanceToNow(activeNotification?.sentAt, { addSuffix: true })}</p>
                            <p className="text-sm font-semibold text-blue-600 uppercase tracking-tight">{activeNotificationData.status}</p>
                        </div>
                    </div>

                    <p className="text-gray-800 leading-relaxed">{activeNotificationData.desc}</p>

                    <div className="mt-6 p-4 bg-gray-50 rounded-xl border border-dashed border-gray-200 flex items-center justify-between">
                        <span className="text-sm font-medium text-gray-600">expense_report_2024.csv</span>
                        <button className="text-blue-600 font-bold text-sm hover:underline">
                            <a href={activeNotification?.csv_file}>Download</a>
                        </button>
                    </div>
                </div>

                <div className="p-4 bg-gray-50 text-right">
                    <button className="px-6 py-2 bg-gray-900 text-white text-sm font-medium rounded-lg">
                        Close
                    </button>
                </div>
            </div>
        </div>
    )
}

export default NotificationPopUp