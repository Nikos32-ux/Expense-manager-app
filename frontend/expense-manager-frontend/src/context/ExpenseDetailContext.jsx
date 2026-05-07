import { createContext, useState } from 'react';

export const ExpenseDetailContext = createContext();

export const ExpenseDetailProvider = ({ children }) => {
    const [showExpenseId, setShowExpenseId] = useState(null);
    return (
        <ExpenseDetailContext.Provider value={{ showExpenseId, setShowExpenseId }}>
            {children}
        </ExpenseDetailContext.Provider>
    );
};