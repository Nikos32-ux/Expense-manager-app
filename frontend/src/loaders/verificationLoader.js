import { redirect } from "react-router-dom";
import api from "../axiosClientApi/axios";
import { queryClient } from "../context/queryClient";
import { verifyUser } from "../queries/authQuery";


export const verificationLoader = async ({ request }) => {
    try {
        const path = new URL(request.url).pathname;
        const user = await queryClient.ensureQueryData(verifyUser());

        if (user && (path === "/login" || path === "/register" || path === "/")) {
            return redirect("/dashboard");
        }

        return user;
    }
    catch (error) {
        console.log("no auth user");
        
        const path = new URL(request.url).pathname;
        if (path.startsWith("/dashboard") || path.startsWith("/profile") || path.startsWith("/transactions") || path.startsWith("/categories")) {
            throw redirect("/");
        }
        return null;
    }
}