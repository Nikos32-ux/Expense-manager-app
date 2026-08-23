import React from 'react'
import { useInfiniteQuery, useQuery } from '@tanstack/react-query';
import api from '../axiosClientApi/axios';


const useAdminUsers = ({ searchInput }) => {

    const fetchUserByEmail = async (searchInput) => {
        console.log("fetch user by email is triggered")

        const result = await api.get(`admin/users/${searchInput}`);
        console.log("user by email response", result.data)
        return result.data;
    }

    const { data: adminSearchUserData, isLoading: adminSearchUserDataLoading, isSuccess: adminSearchUserDataSuccess } = useQuery({
        queryKey: ["single-user-details", searchInput],
        queryFn: () => fetchUserByEmail(searchInput),
        enabled: !!searchInput
    })


    const fetchUsersData = async (pageParam) => {
        console.log("fetch users data is triggered")

        const result = await api.get("admin/users",
            { params: { page: pageParam } }
        );
        console.log(" users data: ", result.data)

        return result.data;
    }

    const { data: adminUsersData = { pages: [], pageParams: [] }, isFetchingNextPage: adminUsersDataIsFetchingNextPage, isLoading: adminUsersDataLoading, isFetching, hasNextPage, fetchNextPage, status, isError, error } = useInfiniteQuery({
        queryKey: ["users"],
        queryFn: ({ pageParam }) => fetchUsersData(pageParam),
        initialPageParam: 0,
        getNextPageParam: (lastPage) => {
            if (lastPage.last) {
                return undefined;
            }
            return lastPage.number + 1;
        }
    })


    return { adminUsersData, adminUsersDataIsFetchingNextPage, adminUsersDataLoading, hasNextPage, fetchNextPage, adminSearchUserData, adminSearchUserDataLoading, adminSearchUserDataSuccess }
}

export default useAdminUsers