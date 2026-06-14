import { z } from "zod";


export const addExpenseSchema = z.object({
    amount: z.coerce.number({ invalid_type_error: "Amount must be a valid number" })
        .positive("Amount must be positive"),
    description: z.string()
        .min(1, "Description required")
        .max(255, "Maximum 255 characters"),
    date: z.string()
        .min(1, "Date required"),
    time: z.string()
        .min(1, "Time required"),
    categoryId: z.coerce.number({ invalid_type_error: "Please select a valid category" })
        .int()
        .positive("Choose a category"),
    payment: z.string().refine((value) => {
        return value === "cash" || value === "card";
    }, {
        message: "Please select a payment method"
    })
})
