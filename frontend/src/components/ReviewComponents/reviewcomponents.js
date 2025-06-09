import React, { useEffect, useState } from 'react';

function ReviewUI({ movieId = 2 }) {
    const [reviews, setReviews] = useState([]);
    const [loading, setLoading] = useState(true);
    const [newReview, setNewReview] = useState({
        rating: '',
        comment: '',
        spoilerAlert: false,
    });
    const [submitting, setSubmitting] = useState(false);

    useEffect(() => {
        console.log('movieId:', movieId);

        if (!movieId) {
            console.warn('movieId không được truyền vào component ReviewUI');
            setLoading(false);
            return;
        }

        fetch(`http://localhost:8081/api/public/reviews/movie/${movieId}`)
            .then((res) => {
                if (!res.ok) {
                    throw new Error(`Lỗi API: ${res.status}`);
                }
                return res.json();
            })
            .then((data) => {
                if (!Array.isArray(data)) {
                    throw new Error('Dữ liệu không đúng định dạng mảng');
                }

                const movieData = data[0];
                const movieReviews = movieData?.reviews || [];

                console.log('Reviews thực tế:', movieReviews);
                setReviews(movieReviews);
            })
            .catch((error) => {
                console.error('Lỗi khi tải reviews:', error.message);
            })
            .finally(() => {
                setLoading(false);
            });
    }, [movieId]);

    const averageRating = reviews.length
        ? (reviews.reduce((sum, r) => sum + Number(r.rating || 0), 0) / reviews.length).toFixed(1)
        : '0.0';

    const handleInputChange = (e) => {
        const { name, value, type, checked } = e.target;
        setNewReview((prev) => ({
            ...prev,
            [name]: type === 'checkbox' ? checked : value,
        }));
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!newReview.rating || !newReview.comment.trim()) {
            alert('Vui lòng nhập đầy đủ số sao và nội dung!');
            return;
        }

        setSubmitting(true);

        fetch('http://localhost:8081/api/public/reviews', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify({
                movieId,
                accountId: 1,
                rating: Number(newReview.rating),
                comment: newReview.comment,
                spoilerAlert: newReview.spoilerAlert,
            }),
        })
            .then((res) => {
                if (!res.ok) throw new Error('Gửi đánh giá thất bại');
                return res.json();
            })
            .then((newAddedReview) => {
                setReviews((prev) => [...prev, newAddedReview]);
                setNewReview({ rating: '', comment: '', spoilerAlert: false });
            })
            .catch((err) => alert(err.message))
            .finally(() => setSubmitting(false));
    };

    return (
        <div
            className="container-fluid"
            style={{
                backgroundColor: '#000',
                color: '#fff',
                minHeight: '100vh',
                padding: '40px 20px',
            }}
        >
            <div className="w-100 d-flex justify-content-center">
                <div className="mx-auto w-100" style={{ maxWidth: '900px' }}>
                    <hr />
                    <h4 className="fw-bold mb-4">Bình luận từ người xem</h4>

                    {loading ? (
                        <p>Đang tải dữ liệu...</p>
                    ) : (
                        <>
                            <div className="d-flex align-items-center mb-4">
                                <span className="fs-3 text-warning me-2">★</span>
                                <span className="fs-3 fw-bold me-2">{averageRating}</span>
                                <span className="fs-3 fw-bold me-2">/10 · {reviews.length} đánh giá</span>
                            </div>

                            {reviews.map((review) => (
                                <div key={review.id} className="mb-4">
                                    <div className="d-flex align-items-center mb-2">
                                        <img
                                            src={
                                                review.avartar ||
                                                'https://th.bing.com/th/id/OIP.CfjwItjNaprcFge4CBfb4gHaHa?w=195&h=195&c=7&r=0&o=5&dpr=1.3&pid=1.7'
                                            }
                                            alt="avatar"
                                            className="rounded-circle me-3"
                                            width="50"
                                            height="50"
                                        />
                                        <div>
                                            <div className="fw-bold">
                                                {review.accountFullName || 'Ẩn danh'}
                                            </div>
                                            <small className="text-muted">{review.time}</small>
                                            {review.boughtVia && (
                                                <div className="text-pink small">
                                                    ✔ Đã mua qua {review.boughtVia}
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                    <div>
                                        <span className="text-warning fw-bold">★ {review.rating}/10</span>
                                        <span className="fw-semibold ms-2">· Đáng xem</span>
                                    </div>
                                    <div className="mt-2" style={{ color: '#fff' }}>
                                        {review.comment}
                                    </div>
                                    {review.image && (
                                        <div className="mt-2">
                                            <img
                                                src={
                                                    'https://th.bing.com/th/id/OIP.CfjwItjNaprcFge4CBfb4gHaHa?w=195&h=195&c=7&r=0&o=5&dpr=1.3&pid=1.7'
                                                }
                                                alt="review"
                                                className="rounded"
                                                style={{ maxWidth: '100%' }}
                                            />
                                        </div>
                                    )}
                                    <hr />
                                </div>
                            ))}

                            <h5 className="fw-bold mb-3">Viết đánh giá của bạn</h5>
                            <form onSubmit={handleSubmit} className="mb-5">
                                <div className="mb-3">
                                    <label className="text-warning fw-bold mb-2 d-block">★ Đánh giá</label>
                                    <div style={{ fontSize: '1.5rem' }}>
                                        {[...Array(10)].map((_, i) => {
                                            const value = i + 1;
                                            return (
                                                <span
                                                    key={value}
                                                    onClick={() => setNewReview((prev) => ({ ...prev, rating: value }))}
                                                    style={{
                                                        cursor: 'pointer',
                                                        color: value <= newReview.rating ? '#ffc107' : '#6c757d',
                                                        transition: 'color 0.2s',
                                                        marginRight: '5px',
                                                    }}
                                                >
                                                    ★
                                                </span>
                                            );
                                        })}
                                    </div>
                                </div>

                                <div className="mb-3">
                                    <label className="form-label">Bình luận</label>
                                    <textarea
                                        className="form-control"
                                        name="comment"
                                        value={newReview.comment}
                                        onChange={handleInputChange}
                                        rows={3}
                                        required
                                    ></textarea>
                                </div>
                                <button type="submit" className="btn btn-primary" disabled={submitting}>
                                    {submitting ? 'Đang gửi...' : 'Gửi đánh giá'}
                                </button>
                            </form>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
}

export default ReviewUI;
