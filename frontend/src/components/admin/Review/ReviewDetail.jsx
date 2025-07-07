import React, { useEffect, useState, useCallback } from "react";
import { Table, Spin, Pagination, message, Popconfirm, Space, Input, Button } from "antd";
import { StarFilled, SearchOutlined, PlusOutlined } from "@ant-design/icons";
import { Trash2 } from "lucide-react";
import { useParams, useLocation, useNavigate, useOutletContext } from "react-router-dom";
import { getReviewsByMovieId, deleteReviewAPI } from "../../../service/ReviewService";

const ReviewDetail = () => {
  const [reviews, setReviews] = useState([]);
  const [filteredReviews, setFilteredReviews] = useState([]);
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState("");
  const size = 5;
  const { movieId } = useParams();
  const location = useLocation();
  const navigate = useNavigate();
  const { setBreadcrumbItems } = useOutletContext();

  useEffect(() => {
    if (location.pathname.includes(`/admin/review-detail/${movieId}`)) {
      setBreadcrumbItems([
        { title: "Trang chủ", href: "/admin" },
        { title: "Quản lý đánh giá", href: "/admin/review-list" },
        { title: "Đánh giá" },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  const fetchReviews = useCallback(
    async (retries = 3) => {
      setLoading(true);
      try {
        const reviewsRes = await getReviewsByMovieId(movieId, page - 1, size);
        const reviewsData = reviewsRes.data?.result || reviewsRes.result || reviewsRes;
        console.log(`Phản hồi API /reviews/movie/${movieId}:`, reviewsData);

        if (reviewsData && Array.isArray(reviewsData) && reviewsData.length > 0) {
          const mappedReviews = reviewsData.map((item) => ({
            id: item.id,
            content: item.comment,
            rating: item.rating.toFixed(1),
            isDeleted: item.isDeleted,
            account: {
              accountId: item.account?.accountId || null,
              fullName: item.account?.fullName || "Không xác định",
              email: item.account?.email || "Không có email",
            },
          }));
          setReviews(mappedReviews);
          setFilteredReviews(mappedReviews);
        } else {
          setReviews([]);
          setFilteredReviews([]);
          message.warning("Không có đánh giá nào cho phim này");
        }
      } catch (err) {
        if (retries > 0) {
          console.warn(`Thử lại... (${retries} lần còn lại)`);
          return fetchReviews(retries - 1);
        }
        message.error(`Lỗi tải dữ liệu cho phim ${movieId}: ${err.message || "Unknown error"}`);
        console.error(`Lỗi khi lấy dữ liệu cho phim ${movieId}:`, err.response?.data || err);
        setReviews([]);
        setFilteredReviews([]);
      } finally {
        setLoading(false);
      }
    },
    [movieId, page]
  );

  useEffect(() => {
    fetchReviews();
  }, [fetchReviews]);

  const handleSearch = (value) => {
    setSearchText(value);
    const filtered = reviews.filter(
      (review) =>
        review.account.fullName.toLowerCase().includes(value.toLowerCase()) ||
        review.account.email.toLowerCase().includes(value.toLowerCase())
    );
    setFilteredReviews(filtered);
    setPage(1);
  };

  const handleDeleteReview = async (reviewId) => {
    try {
      await deleteReviewAPI(reviewId);
      message.success(`Xóa đánh giá thành công`);
      fetchReviews();
    } catch (err) {
      message.error("Lỗi khi xóa đánh giá: " + (err.message || "Unknown error"));
      console.warn(`Lỗi khi xóa đánh giá ${reviewId}:`, err.response?.data || err);
    }
  };

  const handleViewUserDetail = (accountId) => {
    if (accountId) {
      navigate(`/admin/users-members/${accountId}`);
    } else {
      message.warning("Không có thông tin tài khoản để xem chi tiết");
    }
  };

  const columns = [
    {
      title: "Tài khoản",
      key: "account",
      render: (record) => (
        <div className="flex flex-col">
          <p
            className="font-medium text-gray-500 hover:text-blue-600 cursor-pointer hover:underline transition duration-200"
            onClick={() => handleViewUserDetail(record.account.accountId)}
          >
            {record.account.fullName}
          </p>
          <p className="text-gray-600">{record.account.email}</p>
        </div>
      ),
    },
    {
      title: "Nội dung đánh giá",
      dataIndex: "content",
      key: "content",
      render: (text) => (text ? text : "Không có nội dung"),
    },
    {
      title: "Số sao",
      dataIndex: "rating",
      key: "rating",
      render: (rating) => (
        <span>
          {rating}/10 <StarFilled style={{ color: "#fadb14", marginLeft: 4 }} />
        </span>
      ),
    },
    {
      title: "Hành động",
      key: "action",
      render: (record) => (
        <Space size="large">
          <Popconfirm
            placement="left"
            title="Xóa đánh giá"
            description="Xác nhận xóa?"
            onConfirm={() => handleDeleteReview(record.id)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button type="text" danger icon={<Trash2 size={16} strokeWidth={1.7} />} />
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const pagedReviews = filteredReviews.filter((review) => !review.isDeleted).slice((page - 1) * size, page * size);

  return (
    <div
      style={{
        padding: 24,
        background: "#fff",
        borderRadius: 8,
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
      }}
    >
      <div className="flex justify-between items-center mb-4">
        <div style={{ display: "flex", alignItems: "center" }}>
          <div
            style={{
              padding: "0 12px",
              display: "flex",
              alignItems: "center",
              backgroundColor: "#f5f5f5",
              border: "1px solid #d9d9d9",
              borderRight: "none",
              borderTopLeftRadius: 6,
              borderBottomLeftRadius: 6,
              height: 40,
            }}
          >
            <SearchOutlined style={{ fontSize: 18, color: "#999" }} />
          </div>
          <Input
            size="large"
            placeholder="Tìm kiếm theo tên hoặc email..."
            allowClear
            style={{
              width: "28vw",
              borderTopLeftRadius: 0,
              borderBottomLeftRadius: 0,
            }}
            onChange={(e) => {
              handleSearch(e.target.value);
            }}
          />
        </div>
      </div>

        {loading ? (
              <div className="flex flex-col justify-center items-center gap-3 h-screen">
                <Spin size="large" />
                <span className="text-xl font-semibold">Đang tải dữ liệu...</span>
              </div>
            ) : (
        <>
          <Table
            dataSource={pagedReviews}
            columns={columns}
            rowKey="id"
            pagination={false}
            bordered={false}
            style={{ backgroundColor: "#fff", border: "none" }}
            className="custom-table"
          />
          <div className="flex justify-center mt-4">
            <Pagination
              current={page}
              pageSize={size}
              total={filteredReviews.filter((review) => !review.isDeleted).length}
              showSizeChanger={false}
              showTotal={(total, range) =>
                `${range[0]}-${range[1]} trong ${total} mục`
              }
              onChange={(current) => setPage(current)}
            />
          </div>
        </>
      )}
    </div>
  );
};

export default ReviewDetail;