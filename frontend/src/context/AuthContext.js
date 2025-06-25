import { createContext, useEffect, useState } from "react";

export const AuthContext = createContext();

function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const [isAuthLoaded, setIsAuthLoaded] = useState(false);

    const login = (userData, token) => {
        setToken(token);
        setUser(userData);
        localStorage.setItem("user", JSON.stringify(userData));
        localStorage.setItem("token", token);
    }

    const logout = () => {
        setUser(null);
        setToken(null);
        localStorage.removeItem("token");
        localStorage.removeItem("user");
    }

    useEffect(() => {
        const savedToken = localStorage.getItem("token");
        const savedUser = localStorage.getItem("user");

        if (savedToken && savedUser) {
            setToken(savedToken);
            setUser(JSON.parse(savedUser));
        }
        setIsAuthLoaded(true);
    }, [])

    return (
        <AuthContext.Provider value={{ user, token, login, logout, isAuthLoaded }}>
            {children}
        </AuthContext.Provider>
    )
}

export default AuthProvider 