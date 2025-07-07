import React, { useEffect, useState, useCallback } from "react";
import { Table, Input, Spin, Pagination, message } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { Link, useLocation, useOutletContext } from "react-router-dom";
import { debounce } from "lodash";
import axios from "../../../config/axios";

const BookingList = () => {
  const [movies, setMovies] = useState([]);
  const [searchText, setSearchText] = useState("");
  const [page, setPage] = useState(1);
  const [totalElements, setTotalElements] = useState(0);
  const [loading, setLoading] = useState(false);
  const size = 5;
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();

  useEffect(() => {
    if (location.pathname.includes('/admin/booking-list')) {
      setBreadcrumbItems([
        { title: 'Trang chủ', href: '/admin' },
        { title: 'Quản lý vé' },
        { title: 'Lịch sử đặt vé' },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  // Hàm gọi API với retry
  const fetchMovies = useCallback(
    async (retries = 3) => {
      setLoading(true);
      try {
        const res = await axios.get("/movies/getAll", {
          params: {
            search: searchText,
            page: page - 1, // API thường bắt đầu từ page 0
            size,
          },
        });
        const responseData = res.data || res;
        console.log("Phản hồi API /movies/getAll:", responseData); // Log để debug
        if (responseData && responseData.content && responseData.content.length > 0) {
          const mapped = responseData.content.map((item) => ({
            id: item.id,
            poster: item.smallImage,
            nameVN: item.nameVN,
          }));
          console.log("Dữ liệu phim đã ánh xạ:", mapped); // Log để debug
          setMovies(mapped);
          setTotalElements(responseData.totalElements || mapped.length);
        } else {
          setMovies([]);
          setTotalElements(0);
          message.warning("Không có dữ liệu lịch sử đặt vé");
        }
      } catch (err) {
        if (retries > 0) {
          console.warn(`Thử lại... (${retries} lần còn lại)`);
          return fetchMovies(retries - 1);
        }
        message.error("Lỗi tải dữ liệu: " + (err.message || "Unknown error"));
        console.error("Lỗi API /movies/getAll:", err);
      } finally {
        setLoading(false);
      }
    },
    [searchText, page]
  );

  // Debounce tìm kiếm để tránh gọi API quá nhiều
  const debouncedSearch = useCallback(
    debounce((value) => {
      setSearchText(value);
      setPage(1); // Reset về trang 1 khi tìm kiếm
    }, 300),
    []
  );

  // Gọi API khi searchText hoặc page thay đổi
  useEffect(() => {
    fetchMovies();
  }, [fetchMovies]);

  const columns = [
    {
      title: "STT",
      key: "index",
      render: (text, record, index) => (page - 1) * size + index + 1,
    },
    {
      title: "Poster",
      dataIndex: "poster",
      key: "poster",
      render: (poster, record) => (
        <Link
          to={`/admin/booking-detail/${record.id}`}
          onClick={() => console.log("Chuyển hướng với movieId:", record.id)} // Log để debug
        >
          <img
            src={poster || "https://via.placeholder.com/60"}
            alt={record.nameVN}
            style={{ width: 60, borderRadius: 4 }}
            onError={(e) => {
              e.target.src = "https://via.placeholder.com/60"; // Fallback nếu ảnh lỗi
            }}
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
          onClick={() => console.log("Chuyển hướng với movieId:", record.id)} // Log để debug
        >
          {text}
        </Link>
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
            style={{
              width: "28vw",
              borderTopLeftRadius: 0,
              borderBottomLeftRadius: 0,
            }}
            onChange={(e) => debouncedSearch(e.target.value)}
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

export default BookingList;