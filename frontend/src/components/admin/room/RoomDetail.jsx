import React, { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation, useOutletContext } from "react-router-dom";
import axios from "axios";
import { Button, Card, Col, Row, Space, Tag, Popconfirm, message } from "antd";
import { ArrowLeft, Pencil, Trash2 } from "lucide-react";

export default function RoomDetail() {
  const { id: roomId } = useParams();
  const navigate = useNavigate();
  const [room, setRoom] = useState(null);
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();
  useEffect(() => {
    if (location.pathname.includes('/admin/room-list/room')) {
      setBreadcrumbItems([
        { title: 'Trang chủ' },
        { title: 'Phòng chiếu' },
        { title: 'Chi tiết phòng chiếu' },
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
        message.error("Không thể tải dữ liệu phòng");
      }
    };
    fetchRoom();
  }, [roomId]);

  const handleDelete = async () => {
    try {
      await axios.delete(`http://localhost:8081/api/public/rooms/${roomId}`);
      message.success("Xoá phòng thành công");
      navigate("/admin/room-list");
    } catch (err) {
      console.error("Lỗi khi xóa phòng:", err);
      message.error("Không thể xóa phòng");
    }
  };

  if (!room) return <div className="text-center py-5 text-primary">Đang tải dữ liệu phòng...</div>;

  return (
    <div className="p-4 min-h-screen bg-white">
      <Card bordered={false} className="shadow-sm rounded-2xl">
        <Row gutter={[32, 32]}>
          {/* THÔNG TIN PHÒNG */}
          <Col xs={24} md={8}>
            <Space direction="vertical" size="middle" style={{ width: "100%" }}>
              <h2 className="text-xl font-semibold text-primary">{room.name}</h2>

              <p><strong className="text-gray-500">Số hàng:</strong> {room.rows}</p>
              <p><strong className="text-gray-500">Số cột:</strong> {room.cols}</p>
              <p><strong className="text-gray-500">Tổng ghế:</strong> {room.seats.length}</p>

              <Space>
                <Button
                  icon={<Pencil size={16} />}
                  type="primary"
                  onClick={() => navigate(`/admin/room-list/room/edit/${roomId}`)}
                >
                  Sửa thông tin
                </Button>

                <Popconfirm
                  title="Xác nhận xoá phòng"
                  description="Bạn có chắc muốn xoá phòng này không?"
                  onConfirm={handleDelete}
                  okText="Xoá"
                  cancelText="Huỷ"
                  placement="topLeft"
                >
                  <Button danger icon={<Trash2 size={16} />}>
                    Xoá phòng
                  </Button>
                </Popconfirm>
              </Space>

              <Button
                icon={<ArrowLeft size={16} />}
                onClick={() => navigate("/admin/room-list")}
              >
                Quay lại
              </Button>
            </Space>
          </Col>

          {/* SƠ ĐỒ GHẾ */}
          <Col xs={24} md={16}>
            <div className="text-center bg-blue-500 text-white py-2 rounded mb-4 font-semibold">
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
                  <div
                    key={seat.seatCol + seat.seatRow}
                    className="rounded text-center font-semibold"
                    style={{
                      backgroundColor: seatColors[type],
                      border: "1px solid #ccc",
                      boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                      aspectRatio: "1",
                      display: "flex",
                      alignItems: "center",
                      justifyContent: "center",
                    }}
                  >
                    {seat.seatRow + (parseInt(seat.seatCol) + 1)}
                  </div>
                );
              })}
            </div>

            {/* CHÚ THÍCH */}
            <div className="mt-4 text-center">
              <h5 className="text-primary mb-2">Loại ghế:</h5>
              <Space>
                <Tag color="#e0e0e0" style={{ color: "#000" }}>Thường</Tag>
                <Tag color="#f74551">VIP</Tag>
                <Tag color="#f536db">Đôi</Tag>
              </Space>
            </div>
          </Col>
        </Row>
      </Card>
    </div>
  );
}
