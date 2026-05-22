import api from "../axiosClientApi/axios.js";


export const runVerification = async () => {
    const res = await api.get("/auth/user-verify");
    return res.data;
}

export const verifyUser = () => ({
    queryKey: ["verification"],
    queryFn: runVerification,
    staleTime: 1000 * 60 * 1,
    retry: false,
    refetchOnWindowFocus: false,
    meta: { persist: true }
})