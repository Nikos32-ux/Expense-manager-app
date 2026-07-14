import axios from 'axios';

const api = axios.create({
    baseURL: import.meta.env.VITE_API_URL,
    withCredentials: true,
});

console.error("Base url", api.defaults.baseURL)
api.interceptors.response.use(
    (response) => {
        return response;
    },
    (error) => {
        if (error.response?.status === 401) {
            queryClient.removeQueries({ queryKey: ["verification"] });

            localStorage.removeItem("REACT_QUERY_OFFLINE_CACHE");

            window.location.replace("/login");
        }

        return Promise.reject(error);
    }
)

export default api;