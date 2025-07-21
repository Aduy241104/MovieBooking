import React, { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation, useOutletContext } from "react-router-dom";
import axios from "axios";
import {
  Button, Card, Col, Row, Space, Tag,
  Popconfirm, Divider, Tooltip, notification
} from "antd";
import { ArrowLeftToLine, Trash2, Home, SquarePen } from "lucide-react";

export default function RoomDetail() {
  const { id: roomId } = useParams();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();

  useEffect(() => {
    if (location.pathname.includes("/admin/room-list/room")) {
      setBreadcrumbItems([
        { title: "Trang chủ", href: "/admin" },
        { title: "Phòng chiếu" },
        { title: "Chi tiết phòng chiếu" },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  const seatColors = {
    regular: "#e0e0e0",
    vip: "#f74551",
    couple: "#f536db",
  };

  useEffect(() => {
    const fetchRoom = async () => {
      try {
        const data = await axios.get(`http://localhost:8081/api/public/rooms/${roomId}`);
        setRoom(data.data);
      } catch (err) {
        console.error("Lỗi khi lấy chi tiết phòng:", err);
        notification.error({
          message: "TẢI DỮ LIỆU THẤT BẠI",
          description: "Không thể tải dữ liệu phòng. Vui lòng thử lại sau.",
        });
      }
    };
    fetchRoom();
  }, [roomId]);

  const handleDelete = async () => {
    try {
      await axios.delete(`http://localhost:8081/api/public/rooms/${roomId}`);
      notification.success({
        message: "XOÁ THÀNH CÔNG",
        description: "Phòng đã được xoá thành công.",
      });
      navigate("/admin/room-list");
    } catch (err) {
      console.error("Lỗi khi xóa phòng:", err);
      notification.error({
        message: "XOÁ THẤT BẠI",
        description: "Không thể xoá phòng. Vui lòng thử lại.",
      });
    }
  };

  if (!room) {
    return <div className="text-center py-5 text-primary text-lg">Đang tải dữ liệu phòng...</div>;
  }

  return (
    <div className="p-6 min-h-screen bg-[#f9fafb]">
      <Card className="shadow-md rounded-2xl bg-white border border-gray-200">
        <Row gutter={[32, 32]}>
          {/* Thông tin phòng */}
          <Col xs={24} md={8}>
            <Divider orientation="left">Thông tin phòng</Divider>
            <Card className="shadow p-6 rounded-xl bg-white border border-gray-100">
              <h2 className="text-2xl font-bold mb-4 flex items-center gap-2" style={{ color: "#1677FF" }}>
                <Home className="mt-2" /> {room.name}
              </h2>
              <Row gutter={[0, 8]}>
                <Col span={24}><p><strong className="text-gray-500">Số hàng:</strong> {room.rows}</p></Col>
                <Col span={24}><p><strong className="text-gray-500">Số cột:</strong> {room.cols}</p></Col>
                <Col span={24}><p><strong className="text-gray-500">Tổng ghế:</strong> {room.seats.length}</p></Col>
              </Row>

              <Divider />

              <Space wrap>
                <Button
                  icon={<SquarePen size={16} />}
                  type="primary"
                  onClick={() => navigate(`/admin/room-list/room/edit/${roomId}`)}
                >
                  Sửa
                </Button>

                <Popconfirm
                  title="Xác nhận xoá phòng"
                  description="Bạn có chắc muốn xoá phòng này không?"
                  onConfirm={handleDelete}
                  okText="Xoá"
                  cancelText="Huỷ"
                  placement="topLeft"
                >
                  <Button danger icon={<Trash2 size={16} />}>Xoá</Button>
                </Popconfirm>

                <Button icon={<ArrowLeftToLine size={16} className="mt-1" />} onClick={() => navigate("/admin/room-list")}>
                  Quay lại
                </Button>
              </Space>
            </Card>
          </Col>

          {/* Sơ đồ ghế */}
          <Col xs={24} md={16}>
            <Divider orientation="left">Sơ đồ ghế</Divider>

            <Card className="p-4 bg-white rounded-xl shadow-sm border border-gray-100 mt-2 shadow">
              <div
                className="text-center font-medium text-base mb-4"
                style={{
                  borderRadius: "10px",
                  padding: "10px",
                  margin: "0 auto",
                  color: "#fff",
                  backgroundColor: "#1677FF",
                  width: `${room.cols * 48 + (room.cols - 1) * 8}px`,
                  maxWidth: "100%",
                  boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
                }}
              >
                Màn hình
              </div>

              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: `repeat(${room.cols}, 40px)`,
                  gap: "8px",
                  justifyContent: "center",
                }}
              >
                {room.seats.map((seat) => {
                  const type = seat.seatType?.seatTypeName?.toLowerCase() || "regular";
                  return (
                    <Tooltip
                      key={seat.seatCol + seat.seatRow}
                      title={`Hàng ${seat.seatRow}, Cột ${parseInt(seat.seatCol) + 1} (${seat.seatType?.seatTypeName})`}
                    >
                      <div
                        className="hover:scale-105"
                        style={{
                          backgroundColor: seatColors[type],
                          borderRadius: "10px",
                          aspectRatio: "1",
                          boxShadow: "0 1px 4px rgba(0,0,0,0.15)",
                          display: "flex",
                          alignItems: "center",
                          justifyContent: "center",
                          color: "#333",
                          fontWeight: "500",
                          fontSize: "0.8rem",
                          transition: "all 0.2s ease",
                          cursor: "pointer",
                        }}
                      >
                        {seat.seatRow + (parseInt(seat.seatCol) + 1)}
                      </div>
                    </Tooltip>
                  );
                })}
              </div>

              <div className="d-flex mt-5 justify-content-center">
                <h5 className="text-[#1e40af] mb-2 text-base font-semibold me-3">Loại ghế:</h5>
                <Space wrap>
                  <Tag color="#e0e0e0" style={{ color: "#000" }}>Thường</Tag>
                  <Tag color="#f74551">VIP</Tag>
                  <Tag color="#f536db">Đôi</Tag>
                </Space>
              </div>
            </Card>
          </Col>
        </Row>
      </Card>
    </div>
  );
}
