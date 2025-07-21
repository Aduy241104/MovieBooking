import React, { useState, useEffect } from "react";
import { Table, Input, Spin, Button, Popconfirm, Pagination, message } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import { CalendarPlus, Edit, Trash2, Info } from "lucide-react";
import { useNavigate, useLocation, useOutletContext } from "react-router-dom";

import { CreateShowtimeModal } from "../../../components/admin/Modal/showtimes/CreateShowTimeModal";
import { UpdateShowtimeModal } from "../../../components/admin/Modal/showtimes/UpdateShowtimeModal";
import InfoShowTimeModal from "../../../components/admin/Modal/showtimes/InfoShowTimeModal";

import {
  fetchAllActiveShowtimesAPI,
  deleteShowtimeAPI,
} from "../../../service/ShowtimeService";

const ShowtimeList = () => {
  const [showtimes, setShowtimes] = useState([]);
  const [loading, setLoading] = useState(false);
  const [searchText, setSearchText] = useState("");
  const [page, setPage] = useState(1);
  const [refreshFlag, setRefreshFlag] = useState(false);
  const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
  const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
  const [isInfoModalOpen, setIsInfoModalOpen] = useState(false);
  const [currentShowtime, setCurrentShowtime] = useState(null);
  const size = 5;
  const navigate = useNavigate();
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();

  useEffect(() => {
    if (location.pathname.includes("/admin/showtime-list")) {
      setBreadcrumbItems([
        { title: "Trang chủ", href: "/admin" },
        { title: "Quản lý lịch chiếu" },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  useEffect(() => {
    fetchShowtimes();
  }, [refreshFlag]);

  const fetchShowtimes = async () => {
    setLoading(true);
    try {
      const response = await fetchAllActiveShowtimesAPI();
      const data = response.data || response;
      if (data.status === 1000 || data.status === 200) {
        const mappedData = data.result.map((item) => ({
          id: item.id,
          movieName: item.movie.nameVN,
          roomName: item.cinemaRoom.cinemaRoomName,
          showDate: item.showDateTime.slice(0, 10),
          startTime: item.showDateTime.slice(11, 16),
          endTime: item.endDateTime.slice(11, 16),
          movieId: item.movie.id,
          cinemaRoomId: item.cinemaRoom.cinemaRoomId,
          fareTypeId: item.fareType.id,
          showDateTime: item.showDateTime,
          endDateTime: item.endDateTime,
        }));
        setShowtimes(mappedData);
      } else {
        message.error(data.message || "Lỗi lấy dữ liệu lịch chiếu");
      }
    } catch (error) {
      message.error("Lỗi kết nối API");
    } finally {
      setLoading(false);
    }
  };

  const handleDelete = async (record) => {
    try {
      const res = await deleteShowtimeAPI(record.id);
      const data = res.data || res;
      if (data.status === 1000 || data.status === 200) {
        message.success(`Đã xóa lịch chiếu: ${record.movieName}`);
        setRefreshFlag((prev) => !prev);
      } else {
        message.error(data.message || "Xóa thất bại");
      }
    } catch (error) {
      message.error("Lỗi kết nối khi xóa");
    }
  };

  const handleEdit = (record) => {
    setCurrentShowtime(record);
    setIsUpdateModalOpen(true);
  };

  const handleInfo = (record) => {
    setCurrentShowtime(record);
    setIsInfoModalOpen(true);
  };

  const filteredShowtimes = showtimes.filter((item) =>
    item.movieName.toLowerCase().includes(searchText.toLowerCase())
  );

  const pagedShowtimes = filteredShowtimes.slice((page - 1) * size, page * size);

  const columns = [
    {
      title: "STT",
      key: "index",
      render: (text, record, index) => (page - 1) * size + index + 1,
    },
    {
      title: "Tên phim",
      dataIndex: "movieName",
      key: "movieName",
      render: (text, record) => (
        <span
          onClick={() => navigate(`/admin/film-detail/${record.movieId}`)}
          style={{ cursor: "pointer" }}
          onMouseEnter={(e) => (e.target.style.color = "#0d6efd")}
          onMouseLeave={(e) => (e.target.style.color = "")}
        >
          {text}
        </span>
      ),
    },
    { title: "Phòng chiếu", dataIndex: "roomName", key: "roomName" },
    { title: "Ngày chiếu", dataIndex: "showDate", key: "showDate" },
    { title: "Giờ bắt đầu", dataIndex: "startTime", key: "startTime" },
    { title: "Giờ kết thúc", dataIndex: "endTime", key: "endTime" },
    {
      title: "Hành động",
      key: "actions",
      render: (_, record) => (
        <div style={{ display: "flex", gap: 12 }}>
          <Button
            type="text"
            icon={<Info size={18} color="#1677ff" />}
            onClick={() => handleInfo(record)}
          />
          <Button
            type="text"
            icon={<Edit size={18} color="#1677ff" />}
            onClick={() => handleEdit(record)}
          />
          <Popconfirm
            title="Xác nhận xóa?"
            onConfirm={() => handleDelete(record)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button type="text" danger icon={<Trash2 size={18} />} />
          </Popconfirm>
        </div>
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
            placeholder="Tìm theo tên phim..."
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

        <Button
          type="primary"
          size="large"
          icon={<CalendarPlus size={18} />}
          onClick={() => setIsCreateModalOpen(true)}
        >
          <span style={{ marginLeft: 6 }}>Thêm lịch chiếu</span>
        </Button>
      </div>

      {loading ? (
        <div className="flex justify-center">
          <Spin size="large" />
        </div>
      ) : (
        <>
          <Table
            dataSource={pagedShowtimes}
            columns={columns}
            rowKey="id"
            pagination={false}
            bordered={false}
          />
          <div className="flex justify-center mt-4">
            <Pagination
              current={page}
              pageSize={size}
              total={filteredShowtimes.length}
              showSizeChanger={false}
              showTotal={(total, range) =>
                `${range[0]}-${range[1]} trong ${total} mục`
              }
              onChange={setPage}
            />
          </div>
        </>
      )}

      <CreateShowtimeModal
        isCreateModalOpen={isCreateModalOpen}
        setIsCreateModalOpen={setIsCreateModalOpen}
        setRefreshFlag={setRefreshFlag}
      />

      <UpdateShowtimeModal
        isUpdateModalOpen={isUpdateModalOpen}
        setIsUpdateModalOpen={setIsUpdateModalOpen}
        showtimeData={currentShowtime}
        setRefreshFlag={setRefreshFlag}
      />

      <InfoShowTimeModal
        visible={isInfoModalOpen}
        showtime={currentShowtime}
        onClose={() => setIsInfoModalOpen(false)}
      />
    </div>
  );
};

export default ShowtimeList;
