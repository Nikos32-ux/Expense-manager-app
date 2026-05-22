import { redirect } from "react-router-dom";
import { queryClient } from "../context/queryClient";
import { verifyUser } from "../queries/authQuery";
import api from "../axiosClientApi/axios";


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

    const cachedUser = queryClient.getQueryData(["verification"]);
    console.log("cached", cachedUser);
    
    if (isPublicRoute){
        if (cachedUser) return redirect("/dashboard");
        return null;
    }

    try {
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