// src/services/reviewService.js
const BASE_URL = 'http://localhost:8081/api/public/reviews';

const ReviewService = {
    async getReviewsByMovieId(movieId) {
        const res = await fetch(`${BASE_URL}/movie/${movieId}`);
        if (!res.ok) throw new Error("Không thể lấy danh sách đánh giá");
        return res.json();
    },

    async addReview(reviewData) {
        const res = await fetch(`${BASE_URL}/add`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(reviewData),
        });
        if (!res.ok) {
            const error = await res.text();
            throw new Error(error || "Lỗi khi gửi đánh giá");
        }
        return res.json();
    },

    async updateReview(reviewId, updatedData) {
        const res = await fetch(`${BASE_URL}/${reviewId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updatedData),
        });
        if (!res.ok) throw new Error("Cập nhật đánh giá thất bại");
        return res.json();
    },

    async deleteReview(reviewId) {
        const res = await fetch(`${BASE_URL}/${reviewId}`, {
            method: "DELETE",
            headers: { "Content-Type": "application/json" },
        });
        if (!res.ok) throw new Error("Xóa đánh giá thất bại");
        return res;
    }
};

export default ReviewService;
