import { useContext, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import getTokenStatus from "../Utils/CheckExpiredToken";
import { showSessionExpiredModal } from "../Utils/showSessionExpiredModal ";
import { AuthContext } from "../context/AuthContext";

function useTokenCheckOnNavigation() {
    const location = useLocation();
    const navigate = useNavigate();
    const {logout} = useContext(AuthContext);

    // 🛡️ Chỉ những đường dẫn này mới được kiểm tra token
    const protectedPaths = ["/booking", "/profile", "/checkout", "/admin"];

    useEffect(() => {
        const currentPath = location.pathname;
        const isProtected = protectedPaths.some(path => currentPath.startsWith(path));

        if (!isProtected) return;

        const token = localStorage.getItem("token");
        const status = getTokenStatus(token, 3);

        switch (status) {
            case "no-token":
            case "expired":
            case "about-to-expire":
            case "invalid":
                showSessionExpiredModal(() => {
                    logout();
                    navigate("/");
                })
                break;
            case "valid":
            default:
                // ✅ Token còn hạn, không cần xử lý
                break;
        }
    }, [location, navigate]);
}

export default useTokenCheckOnNavigation;
