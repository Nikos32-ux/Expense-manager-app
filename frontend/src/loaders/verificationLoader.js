import { redirect } from "react-router-dom";
import api from "../axiosClientApi/axios";
import { queryClient } from "../context/queryClient";
import { verifyUser } from "../queries/authQuery";


export const verificationLoader = async ({ request }) => {
    const path = new URL(request.url).pathname;
    const isPrivateRoute = (
        path.startsWith("/dashboard") ||
        path.startsWith("/profile") ||
        path.startsWith("/transactions") ||
        path.startsWith("/categories")
    );
    const isPublicRoute = (
        path === "/login" || 
        path === "/register" || 
        path === "/"
    );
    
        try {
        
        
        const user = await queryClient.ensureQueryData(verifyUser());

        if(isPublicRoute && user){
            return redirect("/dashboard");
        }
        
        return user;
    }
    catch (error) {   
        const status = error?.response?.status;
        const path = new URL(request.url).pathname;

        if (isPrivateRoute) {
            if (status === 401) throw redirect("/login");
            throw error;
        }
    }
}