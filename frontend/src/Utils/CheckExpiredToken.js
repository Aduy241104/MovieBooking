function getTokenStatus(token, limit = 1) {
    if (!token || token.trim() === "") return "no-token";

    try {
        const payload = JSON.parse(atob(token.split('.')[1]));
        const exp = payload.exp * 1000; // chuyển sang ms
        const now = Date.now();

        if (now > exp) return "expired";
        if (exp - now < limit * 60 * 1000) return "about-to-expire"; // dưới 3 phút
        return "valid";
    } catch {
        return "invalid";
    }
}

export default getTokenStatus;