import { createContext, useState } from 'react';

export const ExpenseDetailContext = createContext();

export const ExpenseDetailProvider = ({ children }) => {
    const [reportStale, setReportStale] = useState(false);
    return (
        <ExpenseDetailContext.Provider value={{ reportStale, setReportStale }}>
            {children}
        </ExpenseDetailContext.Provider>
    );
};