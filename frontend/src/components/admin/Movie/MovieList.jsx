import React, { useState } from "react";
import { Table, Input, Spin, Button } from "antd";
import { SearchOutlined, PlusOutlined } from "@ant-design/icons";

const movies = [
  {
    id: 1,
    poster: "https://cinema.momocdn.net/img/80099757410724750-doemon.png?size=M",
    nameVN: "Doraemon Movie 44: Nobita và Cuộc Phiêu Lưu Vào Thế Giới Trong Tranh",
    genres: "Gia đình, Phiêu lưu, Giả tưởng, Hoạt hình",
    releaseDate: "23/05/2025",
    endDate: "23/06/2025",
  },
  {
    id: 2,
    poster: "https://via.placeholder.com/60",
    nameVN: "Conan: Bản Giao Hưởng Đỏ Thẫm",
    genres: "Hành động, Trinh thám, Hoạt hình",
    releaseDate: "01/06/2025",
    endDate: "30/06/2025",
  },
];

const MovieList = () => {
  const [searchText, setSearchText] = useState("");

  const filteredMovies = movies.filter((movie) =>
    movie.nameVN.toLowerCase().includes(searchText.toLowerCase())
  );

  const columns = [
    {
      title: "STT",
      dataIndex: "index",
      key: "index",
      render: (text, record, index) => index + 1,
    },
    {
      title: "Poster",
      dataIndex: "poster",
      key: "poster",
      render: (poster, record) => (
        <img
          src={poster}
          alt={record.nameVN}
          style={{ width: 60, borderRadius: 4 }}
        />
      ),
    },
    {
      title: "Tên phim (VN)",
      dataIndex: "nameVN",
      key: "nameVN",
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
        {/* Thanh tìm kiếm có icon riêng */}
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
          <span>Thêm phim</span>
        </Button>
      </div>

      {false ? (
        <div className="flex flex-col justify-center items-center gap-3 h-screen">
          <Spin size="large" />
          <span className="text-xl font-semibold">Đang tải dữ liệu...</span>
        </div>
      ) : (
        <Table
          dataSource={filteredMovies}
          columns={columns}
          rowKey="id"
          pagination={false}
          bordered={false}
          style={{ backgroundColor: "#fff", border: "none" }}
          className="custom-table"
        />
      )}
    </div>
  );
};

export default MovieList;
