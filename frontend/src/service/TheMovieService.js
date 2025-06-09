import axios from "axios";

const API_URL = process.env.REACT_APP_BASE_URL;

const axiosInstance = axios.create({
    baseURL: API_URL,
    headers: {
        "Content-Type": "application/json",

    },
});

export function getAuthHeaders() {
    const token = localStorage.getItem("token");
    return token
        ? { Authorization: `Bearer ${token}` }
        : {};
}


export const searchMovieByName = async (keyWord, page, size) => {
    page = page || 0;
    size = size || 4;

    console.log("key: ", keyWord);

    try {
        const response = await axiosInstance.get(`/public/findMovie/search?q=${keyWord}&page=${page}&size=${size}`);
        console.log(response);
        return response.data;
    } catch (error) {
        console.error("Error fetching movie:", error);
        throw new Error("Lỗi kết nối, vui lòng thử lại sau.");
    }
};


export const getNowShowingMovieAPI = async () => {
    try {
        const response = await axiosInstance.get('/public/movieSchedule/now-showing');
        console.log("Now Showing", response);
        return response.data;
    } catch (error) {
        throw new Error('Cannot connect to server!')
    }
}

export const getUpComingMovieAPI = async () => {
    try {
        const response = await axiosInstance.get('/public/movieSchedule/up-coming');
        console.log("Upcomming: ", response);

        return response.data;
    } catch (error) {
        throw new Error('Cannot connect to server!')
    }
}
