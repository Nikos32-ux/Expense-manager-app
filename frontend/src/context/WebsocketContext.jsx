import { createContext, useContext, useEffect, useState } from "react";
import { Client } from "@stomp/stompjs";
import { verifyUser } from "../queries/authQuery";
import { useRouteLoaderData } from "react-router-dom";
import { useQuery } from "@tanstack/react-query";
import { queryClient } from "./queryClient";
import api from "../axiosClientApi/axios";
import { ExpenseDetailContext } from "./ExpenseDetailContext";

export const WebSocketContext = createContext();

export const WebSocketProvider = ({ children }) => {
    const authUser = queryClient.getQueryData(["verification"]);
    const {reportStale, setReportStale} = useContext(ExpenseDetailContext);
    
    const getNotifications = async () => {    
        const res = await api.get("notifications/get-notifications");
        if(res.data.length === 0) {
            setReportStale(true);
        }
        return res.data;
    }

    const { data: notifications = [], isLoading: notificationsIsLoading } = useQuery({
        queryKey: ["notifications"],
        queryFn: getNotifications,
        enabled:!!authUser
    })

    useEffect(() => {
        if (!authUser) return;
        const client = new Client({
            brokerURL: import.meta.env.VITE_WS_URL,
            reconnectDelay: 5000,
            withCredentials: true
        });

        client.onConnect = () => {
            client.subscribe("/user/topic/notifications", (msg) => {
                setReportStale(false);
                const message = JSON.parse(msg.body);
                queryClient.setQueryData(
                    ["notifications"],
                    (old = []) => {
                        const exists = old.some(n => n.id === message.id);
                        return exists ? old : [...old, message];
                    }
                )
            });
        };

        client.onStompError = (frame) => {
            console.error("STOMP ERROR", frame);
        };

        client.onWebSocketError = (err) => {
            console.error("WS ERROR", err);
        };

        client.onDisconnect = () => {
            console.log("DISCONNECTED");
        }

        client.activate();

        return () => client.deactivate();
    }, [authUser]);

    return (
        <WebSocketContext.Provider value={{ notifications }}>
            {children}
        </WebSocketContext.Provider>
    );
};

