import axios from "axios";

const API_URL = "http://localhost:8081/api/public";

const axiosInstance = axios.create({
    baseURL: API_URL, // Base URL chung cho API
    headers: {
        "Content-Type": "application/json",
    },
});

export const searchMovieByName = async (keyWord, page, size) => {
    if (page) {
        page = 1;
    }
    if (size) {
        size = 4;
    }
    console.log("key: ", keyWord);

    try {
        const response = await axiosInstance.get(`/findMovie/search?q=${keyWord}&page=${page}&size=${size}`);
        console.log(response);
        return response.data

    } catch (error) {
        throw new Error("Loi ket noi vui long thu lai sau");
    }
}
