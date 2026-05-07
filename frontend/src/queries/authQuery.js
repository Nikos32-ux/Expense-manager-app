import api from "../axiosClientApi/axios";


export const runVerification = async () => {
    const res = await api.get("/auth/user-verify");
    return res.data;
}

export const verifyUser = () => ({
    queryKey: ["verification"],
    queryFn: runVerification,
    retry:false,
    refetchOnWindowFocus: false
})