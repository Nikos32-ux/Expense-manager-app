
import api from "../axiosClientApi/axios";

export const fetchCategories = async () => {
    const res = await api.get("/expenses/get-categories");
    return res.data;
  }