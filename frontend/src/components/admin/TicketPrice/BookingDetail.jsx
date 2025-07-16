import {
  message,
  Pagination,
  Space,
  Table,
  Spin,
  Button,
  Input,
} from "antd";
import dayjs from "dayjs";
import {
  InfoCircleOutlined,
  SearchOutlined,
} from "@ant-design/icons";
import { useEffect, useState, useCallback } from "react";
import { useNavigate, useParams } from "react-router-dom";
import { fetchAllBookingsAPI } from "../../../service/TicketPriceService";
import BookingDetailModal from "../Modal/TicketPrice/BookingDetailModal";

export const BookingDetail = () => {
  const { movieId } = useParams();
  const [dataUsers, setDataUsers] = useState([]);
  const [filteredDataUsers, setFilteredDataUsers] = useState([]);
  const [page, setPage] = useState(1);
  const [size] = useState(5);
  const [total, setTotal] = useState(0);
  const [loading, setLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedBooking, setSelectedBooking] = useState(null);
  const [searchText, setSearchText] = useState("");

  const navigate = useNavigate();

  const fetchData = useCallback(
    async (retries = 3) => {
      if (!movieId) {
        message.error("Không tìm thấy movieId");
        return;
      }
      setLoading(true);
      try {
        const res = await fetchAllBookingsAPI(movieId);
        if (res.status && res.status >= 400) {
          message.error(res.message || "Lỗi khi tải danh sách bookings");
          return;
        }

        if (Array.isArray(res.result) && res.result.length > 0) {
          const list = res.result; // ⚠️ Dữ liệu gốc từ API
          setDataUsers(list);
          setFilteredDataUsers(list);
          setTotal(list.length);
        } else {
          setDataUsers([]);
          setFilteredDataUsers([]);
          setTotal(0);
          message.warning("Không có dữ liệu bookings");
        }
      } catch (error) {
        if (retries > 0) {
          return fetchData(retries - 1);
        }
        console.error("Lỗi trong fetchData:", error);
        message.error("Lỗi khi tải danh sách bookings");
      } finally {
        setLoading(false);
      }
    },
    [movieId]
  );

  useEffect(() => {
    fetchData();
  }, [fetchData]);

  const handleSearch = (value) => {
    setSearchText(value);
    const filtered = dataUsers.filter(
      (user) =>
        user.account?.fullName?.toLowerCase().includes(value.toLowerCase()) ||
        user.account?.email?.toLowerCase().includes(value.toLowerCase())
    );
    setFilteredDataUsers(filtered);
    setPage(1);
  };

  const handleTableChange = (current) => {
    setPage(current);
  };

  const handleViewUserDetail = (accountId) => {
    if (accountId) {
      navigate(`/admin/users-members/${accountId}`);
    } else {
      message.warning("Không có thông tin tài khoản để xem chi tiết");
    }
  };

  const handleViewBookingDetail = (booking) => {
    setSelectedBooking(booking);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setSelectedBooking(null);
  };

  const columns = [
    {
      title: "STT",
      render: (_, __, index) => (page - 1) * size + index + 1,
    },
    {
      title: "Tài khoản",
      render: (_, record) => (
        <div className="flex flex-col">
          <p
            className="font-medium text-gray-500 hover:text-blue-600 cursor-pointer hover:underline transition duration-200"
            onClick={() => handleViewUserDetail(record.account?.accountId)}
          >
            {record.account?.fullName || "Không xác định"}
          </p>
          <p className="text-gray-600">{record.account?.email || "Không xác định"}</p>
        </div>
      ),
    },
    {
      title: "Tổng số ghế",
      render: (_, record) =>
        record.bookedSeats?.length ?? "Không có dữ liệu",
    },
    {
      title: "Phòng chiếu",
      render: (_, record) =>
        record.screening?.cinemaRoomName || "Không xác định",
    },
    {
      title: "Ngày Đặt",
      render: (_, record) =>
        record.bookingTime
          ? dayjs(record.bookingTime).format("DD/MM/YYYY HH:mm")
          : "Không có dữ liệu",
    },
    {
      title: "Tổng tiền",
      dataIndex: "totalAmount",
      render: (text) =>
        text ? `${text.toLocaleString("vi-VN")} VNĐ` : "Không có dữ liệu",
    },
    {
      title: "Hành động",
      render: (_, record) => (
        <Space size="large">
          <Button
            type="link"
            className="text-blue-600 hover:text-fuchsia-500"
            onClick={() => handleViewBookingDetail(record)}
          >
            <InfoCircleOutlined style={{ fontSize: 16 }} />
          </Button>
        </Space>
      ),
    },
  ];

  const pagedDataUsers = filteredDataUsers.slice(
    (page - 1) * size,
    page * size
  );

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
            onChange={(e) => handleSearch(e.target.value)}
          />
        </div>
      </div>

      {loading ? (
        <div className="flex flex-col justify-center items-center gap-3 h-screen">
          <Spin size="large" />
          <span className="text-xl font-semibold">Đang tải dữ liệu...</span>
        </div>
      ) : pagedDataUsers.length === 0 ? (
        <div className="text-center text-lg">Không có dữ liệu</div>
      ) : (
        <>
          <Table
            columns={columns}
            dataSource={pagedDataUsers}
            rowKey={(record) =>
              record.bookingId || `BK-${record.account?.accountId}-${Math.random()}`
            }
            pagination={false}
            bordered={false}
            style={{ backgroundColor: "#fff", border: "none" }}
            className="custom-table"
          />
          <div className="flex justify-center mt-4">
            <Pagination
              current={page}
              pageSize={size}
              total={total}
              showSizeChanger={false}
              showTotal={(total, range) =>
                `${range[0]}-${range[1]} trong ${total} mục`
              }
              onChange={handleTableChange}
            />
          </div>
        </>
      )}

      <BookingDetailModal
        visible={isModalOpen}
        booking={selectedBooking}
        onClose={handleCloseModal}
      />
    </div>
  );
};

export default BookingDetail;
