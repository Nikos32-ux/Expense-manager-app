import { createContext, useState } from 'react';

export const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [authReady, setAuthReady] = useState(false);
    return (
        <AuthContext.Provider value={{ authReady, setAuthReady }}>
            {children}
        </AuthContext.Provider>
    );
};