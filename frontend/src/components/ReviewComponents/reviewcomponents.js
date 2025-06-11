import { useContext, useState, useEffect } from 'react';
import styles from "./review.module.scss";
import { useParams } from "react-router-dom";
import { AuthContext } from '../../context/AuthContext';

const ReviewBox = () => {
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
    const { movieId } = useParams();

    useEffect(() => {
        fetch(`http://localhost:8081/api/public/reviews/movie/${movieId}`)
            .then((res) => res.json())
            .then((data) => {
                const movie = data.find((m) => m.id === Number(movieId));
                if (movie) {
                    setReviews(movie.reviews.filter((r) => r.approved));
                    console.log("Reviews:", movie.reviews);
                }
                setLoading(false);
            })
            .catch((err) => {
                console.error("Error fetching reviews:", err);
                setLoading(false);
            });
    }, [movieId]);

    const handleChange = (e) => {
        const { name, value, type, checked } = e.target;
        setNewReview((prev) => ({
            ...prev,
            [name]: type === "checkbox" ? checked : value,
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (newReview.comment.length < 10) {
            alert("Vui lòng nhập đánh giá dài ít nhất 10 ký tự!");
            return;
        }

        if (!user) {
            alert("Vui lòng đăng nhập để gửi đánh giá!");
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
            accountId: user.accountId,
            rating: newReview.rating,
            comment: newReview.comment,
            spoilerAlert: newReview.spoilerAlert,
            approved: true,
            reviewDate: formattedDate,
        };

        fetch(`http://localhost:8081/api/public/reviews/add`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload),
        })
            .then(async (res) => {
                if (!res.ok) {
                    const errorText = await res.text();
                    throw new Error("Gửi đánh giá thất bại: " + errorText);
                }
                return res.json();
            })
            .then((data) => {
                setReviews([data, ...reviews]);
                setNewReview({ rating: 10, comment: "", spoilerAlert: false });
            })
            .catch((err) => {
                console.error("Lỗi gửi đánh giá:", err);
                alert("Lỗi khi gửi đánh giá: " + err.message);
            })
            .finally(() => setSubmitting(false));
    };

    const handleEditSubmit = (e, reviewId) => {
        e.preventDefault();

        if (editedComment.length < 10) {
            alert("Đánh giá phải dài ít nhất 10 ký tự!");
            return;
        }

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
            })
            .catch((err) => {
                console.error("Lỗi cập nhật đánh giá:", err);
                alert("Lỗi cập nhật: " + err.message);
            })
            .finally(() => setSubmitting(false));
    };

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
                if (!response.ok) {
                    throw new Error('Xóa đánh giá thất bại');
                }
                setReviews((prev) => prev.filter((r) => r.id !== reviewId));
                setMenuOpenId(null);
            } catch (error) {
                console.error('Error deleting review:', error);
                alert('Có lỗi xảy ra khi xóa đánh giá');
            } finally {
                setSubmitting(false);
            }
        }
    };

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

    return (
        <div className={styles.wrapper}>
            <h3 className={styles.title}>Đánh giá phim</h3>

            <form className={styles.form} onSubmit={handleSubmit}>
                <label>★ Đánh giá:</label>
                {renderStars(
                    newReview.rating,
                    (value) => setNewReview((prev) => ({ ...prev, rating: value })),
                    true
                )}
                <label>Đánh giá:</label>
                <textarea
                    name="comment"
                    value={newReview.comment}
                    onChange={handleChange}
                    className={styles.textarea}
                    placeholder="Viết đánh giá..."
                    required
                />
                <div className={styles.checkboxWrapper}>
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
                <div className={styles.submitWrapper}>
                    <button type="submit" className={styles.button} disabled={submitting}>
                        {submitting ? "Đang gửi..." : "Gửi đánh giá"}
                    </button>
                </div>
            </form>

            {loading ? (
                <p className={styles.empty}>Đang tải đánh giá...</p>
            ) : reviews.length === 0 ? (
                <p className={styles.empty}>Chưa có đánh giá nào.</p>
            ) : (
                <div className={styles.commentList}>
                    {reviews.map((r) => (
                        <div className={styles.commentItem} key={r.id}>
                            <img src={r.avartar} alt="avatar" className={styles.avatar} />
                            <div className={styles.commentContent}>
                                <div className={styles.commentHeader}>
                                    <div className={styles.headerInfo}>
                                        <span className={styles.username}>{r.accountFullName}</span>
                                        <span className={styles.time}>
                                            • {new Date(r.reviewDate).toLocaleString("vi-VN")}
                                        </span>
                                    </div>
                                    {user && r.accountId === user.accountID && (
                                        <div className={styles.editContainer}>
                                            <button
                                                className={styles.menuButton}
                                                onClick={() => setMenuOpenId(menuOpenId === r.id ? null : r.id)}
                                                title="Tùy chọn"
                                            >
                                                ⋮
                                            </button>
                                            {menuOpenId === r.id && (
                                                <div className={styles.menuDropdown}>
                                                    <button
                                                        className={styles.menuItem}
                                                        onClick={() => {
                                                            setEditingReviewId(r.id);
                                                            setEditedComment(r.comment);
                                                            setEditedRating(r.rating);
                                                            setMenuOpenId(null);
                                                        }}
                                                    >
                                                        Chỉnh sửa
                                                    </button>
                                                    <button
                                                        className={styles.menuItem}
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
                                <div className={styles.rating}>
                                    <span style={{ color: "#FFD875" }}>★</span> {r.rating}/10
                                </div>
                                <p>
                                    {r.spoilerAlert && <span className={styles.spoiler}>[Spoiler]</span>}
                                    {r.comment}
                                </p>
                            </div>
                        </div>
                    ))}

                    {editingReviewId && (
                        <form
                            className={styles.editForm}
                            onSubmit={(e) => handleEditSubmit(e, editingReviewId)}
                        >
                            <label>★ Chỉnh sửa đánh giá:</label>
                            {renderStars(editedRating, (value) => setEditedRating(value), true)}
                            <textarea
                                value={editedComment}
                                onChange={(e) => setEditedComment(e.target.value)}
                                required
                                placeholder="Chỉnh sửa đánh giá..."
                                minLength={10}
                            />
                            <div className={styles.submitWrapper}>
                                <button type="submit" className={styles.button} disabled={submitting}>
                                    {submitting ? "Đang cập nhật..." : "Cập nhật"}
                                </button>
                                <button
                                    type="button"
                                    className={styles.cancelButton}
                                    onClick={() => setEditingReviewId(null)}
                                >
                                    Hủy
                                </button>
                            </div>
                        </form>
                    )}
                </div>
            )}
        </div>
    );
};

export default ReviewBox;