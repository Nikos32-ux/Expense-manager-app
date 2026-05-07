import React, { useContext, useEffect } from 'react'
import { useMutation } from '@tanstack/react-query';
import api from '../axiosClientApi/axios';
import toast, { Toaster } from 'react-hot-toast';
import { useTranslation } from 'react-i18next';
import { WebSocketContext } from '../context/WebsocketContext';



const useProfile = ({ setOpenNotificationById }) => {
    const {notifications} = useContext(WebSocketContext);
    const { t, i18n } = useTranslation();

    const handleExportData = async () => {
        const res = await api.post("report/generate-report");
        return res.data;
    }

    useEffect(() => {
        if(notifications.length === 0) return;

        const latestNotification = notifications[notifications.length - 1];
        if(latestNotification.type === "FILE_GENERATED"){
            setOpenNotificationById(latestNotification.id);
        }
    },[notifications]);

    
    const { mutate: exportFileMutate, isPending: exportFileisPending } = useMutation({
        mutationFn: handleExportData,
        onSuccess: (data) => {
            if (data.status === "FRESH") {
                toast.success(t("report-fresh-data"));
            } else if (data.status === "IN_PROGRESS") {
                toast(t("report-wait"));
            } else if (data.status === "TASK_STARTED") {
                toast(t("started-generating-report"))
            }
        },
        onError: () => {
            toast.error(t("failed-to-start-report"));
        }
    });

    return {
        exportFileMutate,
        exportFileisPending
    }
}

export default useProfile