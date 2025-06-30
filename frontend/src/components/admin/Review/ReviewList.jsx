
import React, { useEffect, useState, useCallback } from "react";
import { Table, Input, Spin, Pagination, message } from "antd";
import { SearchOutlined, StarFilled } from "@ant-design/icons";
import { Link, useLocation, useOutletContext } from "react-router-dom";
import { debounce } from "lodash";
import axios from "../../../config/axios";
import { getAverageRatingAndCountByMovieId } from "../../../service/ReviewService";

const ReviewList = () => {
  const [movies, setMovies] = useState([]);
  const [searchText, setSearchText] = useState("");
  const [page, setPage] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const size = 5;
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();

  useEffect(() => {
    if (location.pathname.includes('/admin/review-list')) {
      setBreadcrumbItems([
        { title: 'Trang chủ', href: '/admin' },
        { title: 'Quản lý đánh giá' },
        { title: 'Danh sách đánh giá phim' },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  const fetchMovies = useCallback(
    async (retries = 3) => {
      setLoading(true);
      try {
        const res = await axios.get("/movies/getAll", {
          params: {
            search: searchText,
            page: page - 1,
            size,
          },
        });
        const responseData = res.data || res;
        console.log("Phản hồi API /movies/getAll:", responseData);

        if (responseData && responseData.content && responseData.content.length > 0) {
          const moviesWithReviews = await Promise.all(
            responseData.content.map(async (item) => {
              try {
                const { averageRating, totalApproved } = await getAverageRatingAndCountByMovieId(item.id);
                return {
                  id: item.id,
                  nameVN: item.nameVN,
                  averageRating: averageRating.toFixed(1),
                  totalReviews: totalApproved,
                  hasReviews: totalApproved > 0,
                };
              } catch (err) {
                console.error(`Lỗi khi lấy đánh giá cho phim ${item.id}:`, err.message);
                if (err.response && err.response.status === 404) {
                  message.warning(`Không tìm thấy đánh giá cho phim ${item.nameVN}`);
                } else {
                  message.error(`Lỗi khi lấy đánh giá cho phim ${item.nameVN}: ${err.message}`);
                }
                return {
                  id: item.id,
                  nameVN: item.nameVN,
                  averageRating: 0.0,
                  totalReviews: 0,
                  hasReviews: false,
                };
              }
            })
          );
          setMovies(moviesWithReviews);
          setTotalElements(responseData.totalElements || moviesWithReviews.length);
        } else {
          setMovies([]);
          setTotalElements(0);
          message.warning("Không có dữ liệu phim");
        }
      } catch (err) {
        if (retries > 0) {
          console.warn(`Thử lại... (${retries} lần còn lại)`);
          return fetchMovies(retries - 1);
        }
        message.error("Lỗi tải dữ liệu: " + (err.message || "Unknown error"));
        console.error("Lỗi API /movies/getAll:", err.response || err);
      } finally {
        setLoading(false);
      }
    },
    [searchText, page]
  );

  const debouncedSearch = useCallback(
    debounce((value) => {
      setSearchText(value);
      setPage(1);
    }, 300),
    []
  );

  useEffect(() => {
    fetchMovies();
  }, [fetchMovies]);

  const columns = [
    {
      title: "STT",
      key: "stt",
      width: 60,
      render: (text, record, index) => index + 1 + (page - 1) * size,
    },
    {
      title: "Tên phim",
      dataIndex: "nameVN",
      key: "nameVN",
      render: (text, record) => (
        <Link
          to={`/admin/review-detail/${record.id}`}
          style={{ color: "#333", textDecoration: "none" }}
          onMouseOver={(e) => (e.target.style.color = "#1890ff")}
          onMouseOut={(e) => (e.target.style.color = "#333")}
        >
          {text}
        </Link>
      ),
    },
    {
      title: "Trung bình sao",
      key: "averageRating",
      render: (record) =>
        record.hasReviews ? (
          <span>
            {record.averageRating}/10 <StarFilled style={{ color: "#fadb14", marginLeft: 4 }} /> ({record.totalReviews} đánh giá)
          </span>
        ) : (
          "Chưa có đánh giá"
        ),
    },
  ];

  return (
    <div
      style={{
        padding: 24,
        background: "#fff",
        borderRadius: 8,
        boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
      }}
    >
      <div className="flex items-center mb-4">
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
          placeholder="Tìm kiếm tên phim..."
          allowClear
          style={{
            width: "28vw",
            borderTopLeftRadius: 0,
            borderBottomLeftRadius: 0,
          }}
          onChange={(e) => debouncedSearch(e.target.value)}
        />
      </div>

      {loading ? (
        <div className="flex flex-col justify-center items-center gap-3 h-screen">
          <Spin size="large" />
          <span className="text-xl font-semibold">Đang tải dữ liệu...</span>
        </div>
      ) : movies.length === 0 ? (
        <div className="text-center text-lg">Không tìm thấy phim nào</div>
      ) : (
        <>
          <Table
            dataSource={movies}
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
              total={totalElements}
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

export default ReviewList;
