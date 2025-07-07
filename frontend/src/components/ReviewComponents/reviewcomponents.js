
import { useContext, useState, useEffect } from 'react';
import styles from "./review.module.scss";
import { useParams } from "react-router-dom";
import { AuthContext } from '../../context/AuthContext';
import ReviewService from '../../service/ReviewService';
import { message } from 'antd';
import { Modal } from 'antd';
import { openNotification } from '../../Utils/Notification';

const ReviewBox = () => {
    // --- Khởi tạo State ---
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [submitting, setSubmitting] = useState(false);
    const [hoverRating, setHoverRating] = useState(0);
    const [newReview, setNewReview] = useState({
        rating: 10,
        comment: "",
        spoilerAlert: false,
    });
    const [editingReviewId, setEditingReviewId] = useState(null);
    const [editedComment, setEditedComment] = useState("");
    const [editedRating, setEditedRating] = useState(10);
    const [menuOpenId, setMenuOpenId] = useState(null);

    const { user } = useContext(AuthContext);
    const { id } = useParams();
    const movieId = id;
    const [page, setPage] = useState(0);
    const [hasMore, setHasMore] = useState(true);
    const pageSize = 7;
    const [refreshTrigger, setRefreshTrigger] = useState(0);
    const [isEditModalOpen, setIsEditModalOpen] = useState(false);





    const handleLoadMore = () => {
        setPage((prev) => prev + 1);
    };

    // --- Xem đánh giá: tải danh sách đánh giá của phim ---
    useEffect(() => {
        setLoading(true);
        fetch(`http://localhost:8081/api/public/reviews/movie/${movieId}/paged?page=${page}&size=${pageSize}`)
            .then((res) => res.json())
            .then((data) => {
                console.log(" Dữ liệu phản hồi từ API:", data);
                const content = data.content || [];

                // Nếu là trang đầu, reset lại danh sách
                if (page === 0) {
                    setReviews(content);
                } else {
                    setReviews((prev) => [...prev, ...content]); //  Nối thêm vào danh sách cũ
                }

                // Kiểm tra còn trang nào nữa không
                if (content.length < pageSize || data.pageNumber >= data.totalPages - 1) {
                    setHasMore(false);
                } else {
                    setHasMore(true);
                }

                console.log(" Tổng số trang:", data.totalPages);
                console.log(" Trang hiện tại:", data.pageNumber);
                console.log(" Số review trả về:", content.length);

                setLoading(false);
            })
            .catch((err) => {
                console.error(" Error fetching reviews:", err);
                setLoading(false);
                setHasMore(false);
            });
    }, [movieId, page, refreshTrigger]);



    // --- Xử lý nhập liệu cho đánh giá mới ---
    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setNewReview((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    // --- Thêm đánh giá mới ---
    const handleSubmit = (e) => {
        e.preventDefault();


        if (!user) {
            message.warning(" Vui lòng đăng nhập để gửi đánh giá!");
            return;
        }

        const existingReview = reviews.find(
            (r) => r.accountId === user.accountId && r.movieId === Number(movieId)
        );
        if (existingReview) {
            alert("Bạn đã gửi đánh giá cho phim này rồi!");
            return;
        }

        setSubmitting(true);
        const now = new Date();
        const formattedDate = now.toISOString().split(".")[0];

        const payload = {
            movieId: Number(movieId),
            accountId: user.accountID,
            rating: newReview.rating,
            comment: newReview.comment,
            spoilerAlert: newReview.spoilerAlert,
            approved: true,
            reviewDate: formattedDate,
        };

        if ('id' in payload) {
            delete payload.id;
        }

        console.log("Payload gửi lên:", payload);

        fetch(`http://localhost:8081/api/public/reviews/add`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        })
            .then(async (res) => {
                if (!res.ok) {
                    const errorText = await res.text();
                    throw new Error(errorText || "Đã xảy ra lỗi khi gửi đánh giá.");
                }
                return res.json();
            })
            .then((data) => {
                setReviews([data, ...reviews]);
                setNewReview({ rating: 10, comment: "", spoilerAlert: false });
            })
            .catch((err) => {
                let errorMessage = 'Gửi đánh giá thất bại!';

                try {
                    // Nếu err.message là chuỗi JSON: {"status":400,"message":"..."}
                    const parsed = JSON.parse(err.message);
                    if (parsed?.message) {
                        errorMessage = parsed.message;
                    }
                } catch (e) {
                    // Nếu parse thất bại, giữ nguyên thông báo mặc định
                    console.error("Lỗi parse JSON trong message:", e);
                }

                openNotification("error", errorMessage);
            })




            .finally(() => setSubmitting(false));
    };


    // --- Sửa đánh giá ---
    const handleEditSubmit = (e, reviewId) => {
        e.preventDefault();

        setSubmitting(true);

        const reviewToUpdate = reviews.find((r) => r.id === reviewId);
        const updatedPayload = {
            rating: editedRating,
            comment: editedComment,
            spoilerAlert: reviewToUpdate.spoilerAlert,
            approved: true,
            reviewDate: new Date().toISOString().split(".")[0],
        };

        fetch(`http://localhost:8081/api/public/reviews/${reviewId}`, {
            method: "PUT",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(updatedPayload),
        })
            .then((res) => {
                if (!res.ok) throw new Error("Cập nhật thất bại");
                return res.json();
            })
            .then((updatedReview) => {
                setReviews((prev) =>
                    prev.map((r) => (r.id === reviewId ? updatedReview : r))
                );
                setEditingReviewId(null);
                setIsEditModalOpen(false);
            })
            .catch((err) => {
                console.error("Lỗi cập nhật đánh giá:", err);
                alert("Lỗi cập nhật: " + err.message);
            })
            .finally(() => setSubmitting(false));
    };

    const loadReviews = async () => {
        try {
            const data = await ReviewService.getReviewsByMovieId(movieId);
            if (Array.isArray(data) && data.length > 0) {
                setReviews(data[0].reviews || []);
            } else {
                setReviews([]);
            }
        } catch (err) {
            console.error("Error loading reviews:", err);
        }
    };


    // Gọi loadReviews khi component mount
    useEffect(() => {
        loadReviews();
    }, [movieId]);

    // Hàm xử lý xóa review (đã chỉnh lại)
    const handleDelete = async (reviewId) => {
        if (window.confirm('Bạn có chắc muốn xóa đánh giá này?')) {
            try {
                setSubmitting(true);
                const response = await fetch(`http://localhost:8081/api/public/reviews/${reviewId}`, {
                    method: 'DELETE',
                    headers: {
                        'Content-Type': 'application/json',
                    },
                });

                if (!response.ok)
                    throw new Error('Xóa đánh giá thất bại');

                setPage(0);
                setHasMore(true);
                setRefreshTrigger(prev => prev + 1);

                // Sau khi xóa, load lại danh sách đánh giá từ server
                // await loadReviews();

                // Đóng menu và reset trạng thái
                // setMenuOpenId(null);
            } catch (error) {
                console.error('Error deleting review:', error);
                alert('Có lỗi xảy ra khi xóa đánh giá');
            } finally {
                setSubmitting(false);
            }
        }
    };


    // --- Hiển thị sao đánh giá ---
    const renderStars = (rating, onClick, editable = false) => (
        <div className={styles.starRating}>
            {[...Array(10)].map((_, i) => {
                const value = i + 1;
                const filled = value <= (hoverRating || rating);
                return (
                    <span
                        key={value}
                        className={filled ? styles.starFilled : styles.starEmpty}
                        onClick={editable && onClick ? () => onClick(value) : undefined}
                        onMouseEnter={editable ? () => setHoverRating(value) : undefined}
                        onMouseLeave={editable ? () => setHoverRating(0) : undefined}
                    >
                        ★
                    </span>
                );
            })}
        </div>
    );

    // --- JSX render ---
    return (
        <div className={styles.wrapper}>
            <h3 className={styles.title}>Đánh giá phim</h3>

            <form className={styles.form} onSubmit={handleSubmit}>
                <label className={styles.ratingLabel}
                    style={{
                        color: "#FAF9FA",
                        fontFamily: "Netflix Sans, sans-serif",
                        fontSize: "20px",
                    }}
                >
                    Đánh giá:
                </label>
                {renderStars(
                    newReview.rating,
                    (value) => setNewReview((prev) => ({ ...prev, rating: value })),
                    true
                )}
                <label className={styles.ratingLabel}
                    style={{
                        color: "#FAF9FA",
                        fontFamily: "Netflix Sans, sans-serif",
                        fontSize: "20px",
                    }} >Bình luận:</label>
                <div style={{ padding: "10px 16px 0 16px" }}>
                    <textarea
                        style={{ backgroundColor: '#18181b', color: '#cccccc' }}
                        id="comment"
                        name="comment"
                        value={newReview.comment}
                        onChange={handleChange}
                        className="form-control custom-textarea"
                        placeholder="Viết bình luận..."
                        maxLength={1000}
                        rows={4}
                        required
                    />
                </div>

                <div className={styles.checkboxWrapper} >
                    <label className={styles.checkbox}>
                        <input
                            type="checkbox"
                            name="spoilerAlert"
                            checked={newReview.spoilerAlert}
                            onChange={handleChange}
                        />
                        Cảnh báo spoiler
                    </label>
                </div>
                <div className={styles.submitWrapper} >
                    <button type="submit" className={styles.button} disabled={submitting} style={{
                        backgroundColor: "#22222B", // nền vàng
                        color: "#FFD875",               // chữ đen
                        // border: "none",
                    }}>

                        {submitting ? "Đang gửi..." : "Gửi"}

                        <img
                            src="https://cdn-icons-png.flaticon.com/128/10322/10322482.png"
                            alt="Send icon"
                            width="20"
                            height="20"
                            style={{
                                filter: "invert(75%)",
                                display: "inline-block",
                                paddingLeft: "5px",
                                verticalAlign: "middle"  // optional để chắc chắn canh đều
                            }}
                        />
                    </button>
                </div>
            </form>

            {/* Xem danh sách đánh giá */}
            {loading ? (
                <p className={styles.empty}>Đang tải đánh giá...</p>
            ) : reviews.length === 0 ? (
                <p className={styles.empty}>Chưa có đánh giá nào.</p>
            ) : (
                <div className={styles.commentList}>
                    {reviews.map((r) => (
                        <div className="card mb-3  text-light" key={r.id} style={{ backgroundColor: "#22222B", borderColor: "#2A2A2A" }}>
                            <div className="card-body d-flex">
                                <img
                                    src={r.avatar || "https://tse4.mm.bing.net/th/id/OIP.Kv5Yubx4NNPg5fVApv23wQHaLB?rs=1&pid=ImgDetMain&o=7&rm=3"}
                                    alt="avatar"
                                    className="rounded-circle me-3"
                                    style={{ width: "50px", height: "50px", objectFit: "cover" }}
                                    onError={(e) => {
                                        e.target.onerror = null; // tránh lặp vô hạn nếu ảnh fallback cũng lỗi
                                        e.target.src = "https://tse4.mm.bing.net/th/id/OIP.Kv5Yubx4NNPg5fVApv23wQHaLB?rs=1&pid=ImgDetMain&o=7&rm=3";
                                    }}
                                />

                                <div className="flex-grow-1">
                                    <div className="d-flex justify-content-between align-items-center">
                                        <div>
                                            <div>
                                                <strong>{r.accountFullName || "Người dùng ẩn danh"}</strong>
                                            </div>
                                            <div style={{ fontSize: "0.85em", color: "#666" }}>
                                                {new Date(r.reviewDate).toLocaleString("vi-VN")}
                                            </div>
                                        </div>
                                        {user && r.accountId === user.accountID && (
                                            <div style={{ position: 'relative', display: 'inline-block' }}>
                                                {menuOpenId !== r.id ? (
                                                    <button
                                                        className={styles.menuToggle}
                                                        onClick={() => setMenuOpenId(r.id)}
                                                    >
                                                        ⋮
                                                    </button>
                                                ) : (
                                                    <div className={styles.menuDropdown}>
                                                        <button
                                                            className={styles.menuItem}
                                                            onClick={() => {
                                                                setEditingReviewId(r.id);
                                                                setEditedComment(r.comment);
                                                                setEditedRating(r.rating);
                                                                setIsEditModalOpen(true);
                                                                setMenuOpenId(null);
                                                            }}
                                                        >
                                                            Chỉnh sửa
                                                        </button>
                                                        <button
                                                            className={`${styles.menuItem} ${styles.danger}`}
                                                            onClick={() => handleDelete(r.id)}
                                                            disabled={submitting}
                                                        >
                                                            Xóa
                                                        </button>
                                                    </div>
                                                )}
                                            </div>
                                        )}


                                    </div>
                                    <div className="mt-2">
                                        <span className="text-warning fw-bold">★</span> {r.rating}/10
                                    </div>
                                    <p className="mb-0">
                                        {r.spoilerAlert && (
                                            <span className="badge bg-danger me-1">[Spoiler]</span>
                                        )}
                                        {r.comment}
                                    </p>
                                </div>
                            </div>
                        </div>
                    ))}

                    {/* Nút Xem thêm */}
                    {hasMore && !loading && (
                        <div className={styles.loadMoreWrapper}>
                            <button className={styles.loadMoreButton} onClick={handleLoadMore}>
                                Xem thêm
                            </button>
                        </div>
                    )}

                    {!hasMore && reviews.length > 0 && (
                        <p className={styles.noMore}>Đã hiển thị tất cả đánh giá.</p>
                    )}


                    {/* Form sửa đánh giá */}
                    <Modal
                        open={isEditModalOpen}
                        onCancel={() => {
                            setEditingReviewId(null);
                            setIsEditModalOpen(false);
                        }}
                        footer={null}
                        title=" ⭐ Chỉnh sửa đánh giá"
                        centered
                    >
                        <form
                            className={styles.editForm}
                            onSubmit={(e) => handleEditSubmit(e, editingReviewId)}
                        >
                            {renderStars(editedRating, (value) => setEditedRating(value), true)}
                            <textarea
                                value={editedComment}
                                onChange={(e) => setEditedComment(e.target.value)}
                                required
                                placeholder="Chỉnh sửa đánh giá..."
                                className={styles.editTextarea}
                            />
                            <div className={styles.submitWrapper}>
                                <button type="submit" className={styles.button} disabled={submitting}>
                                    {submitting ? "Đang cập nhật..." : "Cập nhật"}
                                </button>
                                <button
                                    type="button"
                                    className={styles.cancelButton}
                                    onClick={() => {
                                        setEditingReviewId(null);
                                        setIsEditModalOpen(false);
                                    }}
                                >
                                    Hủy
                                </button>
                            </div>
                        </form>
                    </Modal>

                </div>
            )}
        </div>
    );
};

export default ReviewBox;
