import React, { useEffect, useState } from "react";
import { Table, Input, Spin, Pagination, message } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { Link, useLocation, useOutletContext } from "react-router-dom";
import axios from "axios";
import { fetchBookingCountAPI } from "../../../service/TicketPriceService";

const BookingList = () => {
  const [movies, setMovies] = useState([]);
  const [searchText, setSearchText] = useState("");
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const size = 5;

  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();

  const token = localStorage.getItem("token");

  useEffect(() => {
    if (location.pathname.includes("/employee/booking-list") || location.pathname.includes("/admin/booking-list")) {
      setBreadcrumbItems([
        { title: "Trang chủ"},
        { title: "Quản lý phim" },
        { title: "Lịch sử đặt vé" },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  const fetchMovies = async () => {
    setLoading(true);
    try {
      const res = await axios.get("http://localhost:8081/api/movies", {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "multipart/form-data"
        },
      });

      const data = res.data;

      if (Array.isArray(data)) {
        const moviesWithBookingCount = await Promise.all(
          data.map(async (item) => {
            try {
              const countResponse = await fetchBookingCountAPI(item.id);
              const bookingCount =
                countResponse?.result ??
                countResponse?.data?.result ??
                0;

              return {
                id: item.id,
                poster: item.smallImageUrl || "https://via.placeholder.com/60",
                nameVN: item.nameVN || `Phim ${item.id}`,
                bookingCount,
              };
            } catch (error) {
              console.error(`Lỗi khi lấy số lượng hóa đơn cho phim ${item.id}:`, error);
              return {
                id: item.id,
                poster: item.smallImageUrl || "https://via.placeholder.com/60",
                nameVN: item.nameVN || `Phim ${item.id}`,
                bookingCount: 0,
              };
            }
          })
        );
        setMovies(moviesWithBookingCount);
      } else {
        message.warning("Không có dữ liệu phim");
        setMovies([]);
      }
    } catch (err) {
      console.error("Lỗi khi gọi API danh sách phim:", err);
      message.error("Lỗi tải dữ liệu phim: " + (err.message || "Unknown error"));
      setMovies([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMovies();
  }, []);

  const filteredMovies = movies.filter((movie) =>
    movie.nameVN?.toLowerCase().includes(searchText.toLowerCase())
  );

  const pagedMovies = filteredMovies.slice((page - 1) * size, page * size);

  const columns = [
    {
      title: "STT",
      key: "index",
      render: (_, __, index) => (page - 1) * size + index + 1,
    },
    {
      title: "Poster",
      dataIndex: "poster",
      key: "poster",
      render: (poster, record) => (
        <Link to={`/admin/booking-detail/${record.id}`}>
          <img
            src={poster}
            alt={record.nameVN}
            style={{ width: 60, borderRadius: 4 }}
          />
        </Link>
      ),
    },
    {
      title: "Tên phim",
      dataIndex: "nameVN",
      key: "nameVN",
      render: (text, record) => (
        <Link
          to={`/admin/booking-detail/${record.id}`}
          style={{ color: "#333", textDecoration: "none" }}
          onMouseOver={(e) => (e.target.style.color = "#1890ff")}
          onMouseOut={(e) => (e.target.style.color = "#333")}
        >
          {text}
        </Link>
      ),
    },
    {
      title: "Tổng hóa đơn của phim",
      dataIndex: "bookingCount",
      key: "bookingCount",
      render: (text) => text ?? 0,
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
            placeholder="Tìm kiếm tên phim..."
            allowClear
            value={searchText}
            style={{
              width: "28vw",
              borderTopLeftRadius: 0,
              borderBottomLeftRadius: 0,
            }}
            onChange={(e) => {
              setSearchText(e.target.value);
              setPage(1);
            }}
          />
        </div>
      </div>

      {loading ? (
        <div style={{ textAlign: "center", padding: "50px" }}>
          <Spin size="large" />
          <p>Đang tải dữ liệu...</p>
        </div>
      ) : pagedMovies.length === 0 ? (
        <div style={{ textAlign: "center", padding: "50px" }}>
          <p>Không có phim nào phù hợp với tìm kiếm</p>
        </div>
      ) : (
        <>
          <Table
            dataSource={pagedMovies}
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
              total={filteredMovies.length}
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

export default BookingList;
