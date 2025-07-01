import { createContext, useEffect, useState } from "react";
import useAutoRefreshToken from "../hooks/useAutoRefreshToken";

export const AuthContext = createContext();

function AuthProvider({ children }) {
    const [user, setUser] = useState(null);
    const [token, setToken] = useState(null);
    const [isAuthLoaded, setIsAuthLoaded] = useState(false);

    const login = (userData, token, refreshToken) => {
        setToken(token);
        setUser(userData);
        localStorage.setItem("user", JSON.stringify(userData));
        localStorage.setItem("token", token);
        localStorage.setItem("refreshToken", refreshToken);
    }

    const logout = () => {
        setUser(null);
        setToken(null);
        localStorage.removeItem("token");
        localStorage.removeItem("user");
        localStorage.removeItem("refreshToken")
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

    useAutoRefreshToken(token, setToken, logout);

    return (
        <AuthContext.Provider value={ { user, token, login, logout, isAuthLoaded } }>
            { children }
        </AuthContext.Provider>
    )
}

export default AuthProvider 