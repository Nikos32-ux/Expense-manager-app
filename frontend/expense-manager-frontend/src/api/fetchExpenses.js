import api from "../axiosClientApi/axios";

export const fetchDashboardExpenses = async () => {
    const res = await api.get("/expenses/get-dashboard-expenses")
    return res.data.content;
  }