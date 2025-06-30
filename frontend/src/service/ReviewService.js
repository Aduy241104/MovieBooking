import axios from "../config/axios";

// Lấy danh sách đánh giá của một phim
const getReviewsByMovieId = (movieId, page = 0, size = 10) => {
    const URL_BACKEND = `/reviews/movie/${movieId}?page=${page}&size=${size}`;
    return axios.get(URL_BACKEND);
};

// Lấy điểm trung bình và số lượng đánh giá được duyệt của một phim
const getAverageRatingAndCountByMovieId = async (movieId) => {
    const URL_AVERAGE = `/reviews/movie/average-rating/${movieId}`;
    const URL_COUNT = `/reviews/movie/total-approved/${movieId}`;
    try {
        const [averageResponse, countResponse] = await Promise.all([
            axios.get(URL_AVERAGE),
            axios.get(URL_COUNT),
        ]);
        console.log(`Phản hồi API average-rating/${movieId}:`, averageResponse);
        console.log(`Phản hồi API total-approved/${movieId}:`, countResponse);
        return {
            averageRating: averageResponse?.result ?? 0.0,
            totalApproved: countResponse?.result ?? 0,
        };
    } catch (error) {
        console.error(`Lỗi khi lấy đánh giá cho phim ${movieId}:`, error);
        throw new Error("Lỗi kết nối, vui lòng thử lại sau.");
    }
};

// Xóa mềm một đánh giá
const deleteReviewAPI = (reviewId) => {
    const URL_BACKEND = `/reviews/${reviewId}`;
    return axios.delete(URL_BACKEND);
};

// Cập nhật một đánh giá
const updateReviewAPI = (reviewId, reviewRequest) => {
    const URL_BACKEND = `/reviews/${reviewId}`;
    return axios.put(URL_BACKEND, reviewRequest);
};

export {
    getReviewsByMovieId,
    getAverageRatingAndCountByMovieId,
    deleteReviewAPI,
    updateReviewAPI,
};