export function getTokenStatus(token, limitMinutes = 3) {
    if (!token || typeof token !== "string" || token.trim() === "") {
        return "no-token";
    }

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const exp = payload.exp * 1000; // Chuyển từ giây → ms
        const now = Date.now();

        if (now >= exp) return "expired";
        if (exp - now < limitMinutes * 60 * 1000) return "about-to-expire";
        return "valid";
    } catch (err) {
        return "invalid";
    }
}

export function getPayload(token) {
    try {
        return JSON.parse(atob(token.split('.')[1]));
    } catch {
        return {};
    }
}


