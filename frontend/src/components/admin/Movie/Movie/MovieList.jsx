import React, { useEffect, useState } from "react";
import { Table, Input, Spin, Button, Pagination, message, Popconfirm, Space } from "antd";
import { SearchOutlined, PlusOutlined } from "@ant-design/icons";
import { SquarePen, Trash2 } from "lucide-react";
import { Link, useLocation, useOutletContext, useNavigate } from "react-router-dom";
import axios from "axios";

const MovieList = () => {
  const [movies, setMovies] = useState([]);
  const [searchText, setSearchText] = useState("");
  const [page, setPage] = useState(1);
  const [loading, setLoading] = useState(false);
  const size = 5;

  const location = useLocation();
  const navigate = useNavigate();
  const { setBreadcrumbItems } = useOutletContext();

  const token = localStorage.getItem('token')
  console.log(">>> Token: " + token)

  useEffect(() => {
    if (location.pathname.includes('/admin/movies')) {
      setBreadcrumbItems([
        { title: 'Trang chủ', href: '/admin' },
        { title: 'Quản lý phim' },
        { title: 'Phim' },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  const fetchMovies = async () => {
    setLoading(true);
    try {
      const res = await axios.get("http://localhost:8081/api/movies", {
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "multipart/form-data"
        },
      });
      if (res && Array.isArray(res.data)) {
        const mapped = res.data.map((item) => ({
          id: item.id,
          poster: item.smallImageUrl || "https://via.placeholder.com/60",
          nameVN: item.nameVN,
          genres: item.typeNames?.join(', ') || "Đang cập nhật",
          releaseDate: item.fromDate,
          endDate: item.toDate,
        }));
        setMovies(mapped);
      } else {
        message.warning("Không có dữ liệu phim");
      }
    } catch (err) {
      console.error("Lỗi khi gọi API:", err);
      message.error("Lỗi tải dữ liệu: " + (err.message || "Unknown error"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchMovies();
  }, []);

  const handleEdit = (record) => {
    navigate(`/admin/movies/edit/${record.id}`);
  };

  const handleDelete = async (id) => {
    try {
      await axios.delete(`http://localhost:8081/api/movies/${id}`, {
        headers: {
          "Authorization": `Bearer ${token}`,
          "Content-Type": "multipart/form-data"
        },
      });
      message.success("Xóa phim thành công");
      fetchMovies();
    } catch (err) {
      console.error("Lỗi khi xoá phim:", err);
      message.error("Xoá phim thất bại");
    }
  };

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
        <Link to={`/admin/film-detail/${record.nameVN}`}>
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
          to={`/admin/film-detail/${record.id}`}
          style={{ color: "#333", textDecoration: "none" }}
          onMouseOver={(e) => (e.target.style.color = "#1890ff")}
          onMouseOut={(e) => (e.target.style.color = "#333")}
        >
          {text}
        </Link>
      ),
    },
    {
      title: "Thể loại",
      dataIndex: "genres",
      key: "genres",
    },
    {
      title: "Ngày khởi chiếu",
      dataIndex: "releaseDate",
      key: "releaseDate",
    },
    {
      title: "Ngày kết thúc",
      dataIndex: "endDate",
      key: "endDate",
    },
    {
      title: "Hành động",
      key: "actions",
      render: (_, record) => (
        <Space size="large">
          <button
            className="text-blue-600 hover:text-fuchsia-500"
            onClick={() => handleEdit(record)}
          >
            <SquarePen size={16} strokeWidth={1.7} />
          </button>
          <Popconfirm
            title="Xác nhận xóa?"
            onConfirm={() => handleDelete(record.id)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button type="text" danger icon={<Trash2 size={16} strokeWidth={1.7} />} />
          </Popconfirm>
        </Space>
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
            onChange={(e) => {
              setSearchText(e.target.value);
              setPage(1);
            }}
          />
        </div>

        <Button type="primary" size="large" onClick={() => navigate("/admin/movies/add")}>
          <PlusOutlined />
          <span>Thêm phim</span>
        </Button>
      </div>

      {loading ? (
        <div className="flex flex-col justify-center items-center gap-3 h-screen">
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

export default MovieList;
