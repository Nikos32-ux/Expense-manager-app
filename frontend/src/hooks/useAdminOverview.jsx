import React from 'react'
import { useQuery, useInfiniteQuery } from '@tanstack/react-query';
import api from '../axiosClientApi/axios';


const useAdminOverview = () => {
    const adminUserLogsData = async () => {
        const res = await api.get("admin/audit-logs");
        console.log("auditLogsData admin data response: ", res.data);
        return res.data;
    }

    const { data: auditLogsData, isLoading: auditLogsDataLoading } = useQuery({
        queryKey: ["audit-logs"],
        queryFn: adminUserLogsData
    })

    const adminOverviewData = async () => {
        const res = await api.get("admin/overview");
        console.log("overview admin data response: ", res.data);
        return res.data;
    }

    const { data: overviewData, isLoading: overviewDataLoading } = useQuery({
        queryKey: ["admin-overview"],
        queryFn: adminOverviewData
    })


    const fetchUserActionsLogs = async(pageParam) => {
        const result = await api.get("admin/audit-logs",
            {params: {page : pageParam}}
        );
        return result.data;
    }

    const { data: userActionLogs = { pages: [], pageParams: [] }, isFetchingNextPage, isLoading: userActionLogsLoading, isFetching, hasNextPage, fetchNextPage, status, isError, error } = useInfiniteQuery({
        queryKey: ["user-actions-logs"],
        queryFn: ({ pageParam }) => fetchUserActionsLogs(pageParam),
        initialPageParam: 0,
        getNextPageParam: (lastPage) => {
            if (lastPage.last) {
                return undefined;
            }
            return lastPage.number + 1;
        }
    })

    return { overviewData, overviewDataLoading, auditLogsData, auditLogsDataLoading, userActionLogs, fetchNextPage, isError, isFetching,isFetchingNextPage, hasNextPage, userActionLogsLoading}
}

export default useAdminOverview