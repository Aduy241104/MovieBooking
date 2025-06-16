import React, { useState, useEffect } from "react";
import { Table, Input, Spin, Button, Pagination, message } from "antd";
import { SearchOutlined, PlusOutlined } from "@ant-design/icons";
import axios from "axios";
import { useNavigate } from "react-router-dom";

const MovieList = () => {
  const [movies, setMovies] = useState([]);
  const [filteredMovies, setFilteredMovies] = useState([]);
  const [searchText, setSearchText] = useState("");
  const [page, setPage] = useState(1);
  const size = 5;
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const fetchMovies = async () => {
    setLoading(true);
    try {
      const res = await axios.get("/api/public/findMovie/getAll");
      const data = res.data.result;
      setMovies(data);
      setFilteredMovies(data);
    } catch (err) {
      message.error("Lỗi tải danh sách phim");
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMovies();
  }, []);

  useEffect(() => {
    const filtered = movies.filter((movie) =>
      movie.nameVN.toLowerCase().includes(searchText.toLowerCase())
    );
    setFilteredMovies(filtered);
    setPage(1);
  }, [searchText, movies]);

  const columns = [
    {
      title: "STT",
      key: "index",
      render: (text, record, index) => (page - 1) * size + index + 1,
    },
    {
      title: "Poster",
      dataIndex: "smallImage",
      key: "smallImage",
      render: (url, record) => (
        <img
          src={url}
          alt={record.nameVN}
          style={{ width: 60, borderRadius: 4, cursor: "pointer" }}
          onClick={() => navigate(`/admin/film-detail/${record.id}`)}
        />
      ),
    },
    {
      title: "Tên phim (VN)",
      dataIndex: "nameVN",
      key: "nameVN",
      render: (text, record) => (
        <span
          onClick={() => navigate(`/admin/film-detail/${record.id}`)}
          style={{ cursor: "pointer" }}
          onMouseEnter={(e) => (e.target.style.color = "#0d6efd")}
          onMouseLeave={(e) => (e.target.style.color = "")}
        >
          {text}
        </span>
      ),
    },
    {
      title: "Diễn viên",
      dataIndex: "actor",
      key: "actor",
      render: (actor) => actor || "N/A",
    },
    {
      title: "Ngày khởi chiếu",
      dataIndex: "fromDate",
      key: "fromDate",
    },
    {
      title: "Ngày kết thúc",
      dataIndex: "toDate",
      key: "toDate",
    },
  ];

  const pagedMovies = filteredMovies.slice((page - 1) * size, page * size);

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
            onChange={(e) => setSearchText(e.target.value)}
          />
        </div>

        <Button type="primary" size="large">
          <PlusOutlined />
          <span style={{ marginLeft: 6 }}>Thêm phim</span>
        </Button>
      </div>

      {loading ? (
        <div className="flex flex-col justify-center items-center gap-3 h-60">
          <Spin size="large" />
          <span className="text-xl font-semibold">Đang tải dữ liệu...</span>
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

export default MovieList;
