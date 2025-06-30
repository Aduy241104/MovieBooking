import { message, Pagination, Space, Table, Spin, Button, Input } from "antd";
import dayjs from "dayjs";
import { InfoCircleOutlined, SearchOutlined } from "@ant-design/icons";
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
      console.log(`Bắt đầu gọi API với movieId: ${movieId}`);
      try {
        const res = await fetchAllBookingsAPI(movieId);
        console.log("Phản hồi đầy đủ từ API:", JSON.stringify(res, null, 2));
        if (res.status && res.status !== 1000) {
          console.error("API trả về lỗi:", res);
          message.error(res.message || "Lỗi khi tải danh sách bookings");
          return;
        }
        if (res.result && res.result.length > 0) {
          const list = res.result.map((booking) => {
            if (!booking.account || !booking.screening || !booking.screening.cinemaRoom) {
              console.warn("Booking thiếu dữ liệu:", booking);
              return null;
            }
            return {
              bookingId: booking.id || `BK-${Math.random().toString(36).slice(2, 8).toUpperCase()}`,
              accountId: booking.account?.accountId,
              fullName: booking.account?.fullName,
              email: booking.account?.email,
              seatCount: booking.seatCount,
              cinemaRoomName: booking.screening.cinemaRoom?.cinemaRoomName,
              bookingTime: booking.bookingTime,
              totalAmount: booking.totalAmount,
              movieName: booking.screening.movie?.nameVN || booking.screening.movie?.nameEN || "Không xác định",
              seats: booking.seats || [],
              promotionCodeApplied: booking.promotionCodeApplied || "Không áp dụng",
              paymentMethod: booking.paymentMethod || null,
              discountApplied: booking.discountApplied || 0,
            };
          }).filter((item) => item !== null);
          console.log("Dữ liệu sau khi xử lý:", list);
          setDataUsers(list);
          setFilteredDataUsers(list);
          setTotal(list.length); // Không có res.totalElements, sử dụng list.length
        } else {
          console.warn("Phản hồi API không có result:", res);
          setDataUsers([]);
          setFilteredDataUsers([]);
          setTotal(0);
          message.warning("Không có dữ liệu ");
        }
      } catch (error) {
        if (retries > 0) {
          console.warn(`Thử lại... (${retries} lần còn lại)`);
          return fetchData(retries - 1);
        }
        console.error("Lỗi trong fetchData:", {
          message: error.message,
          status: error.status,
          data: error.data,
        });
        message.error("Lỗi khi tải danh sách bookings");
      } finally {
        setLoading(false);
      }
    },
    [movieId]
  );

  useEffect(() => {
    fetchData();
  }, [fetchData, page]);

  const handleSearch = (value) => {
    setSearchText(value);
    const filtered = dataUsers.filter(
      (user) =>
        user.fullName?.toLowerCase().includes(value.toLowerCase()) ||
        user.email?.toLowerCase().includes(value.toLowerCase())
    );
    setFilteredDataUsers(filtered);
    setPage(1);
  };

  const handleTableChange = (current) => {
    console.log("Thay đổi phân trang:", current);
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
    console.log("Dữ liệu booking trước khi mở modal:", booking);
    setSelectedBooking(booking);
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    // Không đặt setSelectedBooking(null) ngay lập tức để tránh mất dữ liệu
  };

  const columns = [
    {
      title: "STT",
      render: (_, __, index) => (page - 1) * size + index + 1,
    },
    {
      title: "Tài khoản",
      dataIndex: "fullName",
      render: (text, record) => (
        <div className="flex flex-col">
          <p
            className="font-medium text-gray-500 hover:text-blue-600 cursor-pointer hover:underline transition duration-200"
            onClick={() => handleViewUserDetail(record.accountId)}
          >
            {text || "Không xác định"}
          </p>
          <p className="text-gray-600">{record.email || "Không xác định"}</p>
        </div>
      ),
    },
    {
      title: "Tổng số ghế",
      dataIndex: "seatCount",
      render: (text) => text ?? "Không có dữ liệu",
    },
    {
      title: "Phòng chiếu",
      dataIndex: "cinemaRoomName",
      render: (text) => text || "Không xác định",
    },
    {
      title: "Ngày Đặt",
      dataIndex: "bookingTime",
      render: (text) =>
        text ? dayjs(text).format("DD/MM/YYYY HH:mm") : "Không có dữ liệu",
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

  const pagedDataUsers = filteredDataUsers.slice((page - 1) * size, page * size);

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
            rowKey="bookingId"
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