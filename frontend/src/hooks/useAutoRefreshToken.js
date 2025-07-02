import { useEffect } from "react";
import { getPayload, getTokenStatus } from "../Utils/CheckExpiredToken";
import { showSessionExpiredModal } from "../Utils/showSessionExpiredModal ";

function useAutoRefreshToken(token, setToken, logout) {
    useEffect(() => {
        
        if (!token) return;

        const status = getTokenStatus(token, 3); // 3 phút trước khi hết hạn
        const payload = getPayload(token);

        //Gọi làm mới ngay nếu token đã/đang hết hạn
        if (status === "expired" || status === "invalid" || status === "about-to-expire") {
            refreshAccessToken();
        } else if (status === "valid") {
            //Lên lịch tự refresh nếu token còn hạn
            const exp = payload.exp * 1000;
            const now = Date.now();
            const timeUntilRefresh = exp - now - 3 * 60 * 1000;

            const timer = setTimeout(() => {
                refreshAccessToken();
            }, timeUntilRefresh);

            return () => clearTimeout(timer); // cleanup
        }

        function refreshAccessToken() {
            const refreshToken = localStorage.getItem("refreshToken");
            if (!refreshToken) {
                logout();
                showSessionExpiredModal();
                return;
            }

            fetch("http://localhost:8081/api/auth/refresh", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ refreshToken }),
            })
                .then((res) => {
                    if (!res.ok) throw new Error("Refresh token expired");
                    return res.json();
                })
                .then((data) => {
                    if (data.accessToken) {
                        setToken(data.accessToken);
                        localStorage.setItem("token", data.accessToken);
                    } else {
                        throw new Error("No access token returned");
                    }
                })
                .catch(() => {
                    logout();
                    showSessionExpiredModal();
                });
        }
    }, [token]);
}

export default useAutoRefreshToken;
