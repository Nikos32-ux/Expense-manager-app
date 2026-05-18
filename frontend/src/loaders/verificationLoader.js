import { redirect } from "react-router-dom";
import api from "../axiosClientApi/axios";
import { queryClient } from "../context/queryClient";
import { verifyUser } from "../queries/authQuery";


export const verificationLoader = async ({ request }) => {
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
        const path = new URL(request.url).pathname;
        const cached = queryClient.getQueryData(["verification"]);

        if (isPublicRoute) {
            if (!cached) return null;
            return redirect("/dashboard");
        }
        
        const user = await queryClient.ensureQueryData(verifyUser());
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