import React, { useState, useEffect } from "react";
import axios from "axios";
import { Input, Button, Card, Row, Col, Divider, Tooltip, Space, Tag, notification } from "antd";
import { SquarePen, X } from "lucide-react";

const seatColors = ["#e0e0e0", "#f74551", "#f536db"];

export default function RoomForm({ room, onBack }) {
  const [name, setName] = useState("");
  const [rows, setRows] = useState(0);
  const [cols, setCols] = useState(0);
  const [seatTypes, setSeatTypes] = useState({});
  const [error, setError] = useState("");

  const token = localStorage.getItem("token");

  useEffect(() => {
    if (room) {
      axios
        .get(`http://localhost:8081/api/rooms/${room.id}`, {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        })
        .then((res) => {
          const data = res.data;
          const rowCount = data.rows;
          const colCount = data.cols;
          const seatsFromAPI = {};

          data.seats.forEach((seat) => {
            const rowChar = seat.seatRow;
            const colNumber = parseInt(seat.seatCol) + 1;
            const code = rowChar + colNumber;
            const typeName = seat.seatType?.seatTypeName?.toLowerCase() || "regular";
            seatsFromAPI[code] = typeName === "vip" ? 1 : typeName === "couple" ? 2 : 0;
          });

          const completeSeats = {};
          for (let r = 0; r < rowCount; r++) {
            for (let c = 0; c < colCount; c++) {
              const code = String.fromCharCode(65 + r) + (c + 1);
              completeSeats[code] = seatsFromAPI[code] ?? (r < 4 ? 0 : r === rowCount - 1 ? 2 : 1);
            }
          }

          setName(data.name);
          setRows(rowCount);
          setCols(colCount);
          setSeatTypes(completeSeats);
        })
        .catch((err) => {
          console.error("Lỗi khi load phòng:", err);
          notification.error({
            message: "TẢI DỮ LIỆU THẤT BẠI",
            description: "Không thể tải dữ liệu phòng.",
          });
        });
    }
  }, [room, token]);

  const generateSeats = () => {
    const seats = {};
    for (let r = 0; r < rows; r++) {
      for (let c = 0; c < cols; c++) {
        const code = String.fromCharCode(65 + r) + (c + 1);
        seats[code] = r < 4 ? 0 : r === rows - 1 ? 2 : 1;
      }
    }
    setSeatTypes(seats);
  };

  const toggleSeatType = (code) => {
    setSeatTypes((prev) => ({ ...prev, [code]: (prev[code] + 1) % 3 }));
  };

  const validateName = async () => {
    if (!name.trim()) {
      setError("Tên phòng không được để trống.");
      return false;
    }

    try {
      const res = await axios.get("http://localhost:8081/api/rooms", {
        headers: {
          Authorization: `Bearer ${token}`,
          "Content-Type": "application/json",
        },
      });
      const existingRooms = res.data;

      const isDuplicate = existingRooms.some(
        (r) =>
          r.cinemaRoomName.toLowerCase() === name.trim().toLowerCase() &&
          (!room || r.cinemaRoomId !== room.id)
      );

      if (isDuplicate) {
        setError("Tên phòng đã tồn tại.");
        return false;
      }
    } catch (err) {
      notification.error({
        message: "KIỂM TRA TÊN PHÒNG THẤT BẠI",
        description: "Không thể kiểm tra tên phòng.",
      });
      return false;
    }

    setError("");
    return true;
  };

  const handleSubmit = async () => {
    const isValid = await validateName();
    if (!isValid) return;

    const seats = Object.keys(seatTypes).map((code) => ({
      seatRow: code.charCodeAt(0) - 65,
      seatCol: parseInt(code.slice(1)) - 1,
      seatType: seatTypes[code] === 0 ? "regular" : seatTypes[code] === 1 ? "vip" : "couple",
    }));

    const payload = { name, rows, cols, seats };

    try {
      if (room) {
        await axios.put(`http://localhost:8081/api/rooms/${room.id}`, payload, {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });
        notification.success({
          message: "CẬP NHẬT THÀNH CÔNG",
          description: "Cập nhật phòng chiếu thành công.",
        });
      } else {
        await axios.post("http://localhost:8081/api/rooms", payload, {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });
        notification.success({
          message: "THÊM PHÒNG THÀNH CÔNG",
          description: "Thêm phòng chiếu mới thành công.",
        });
      }
      onBack();
    } catch (err) {
      console.error("Lỗi khi lưu:", err);
      notification.error({
        message: "LỖI KHI LƯU",
        description: "Đã xảy ra lỗi khi lưu phòng chiếu.",
      });
    }
  };

  return (
    <div className="p-6 min-h-screen bg-white">
      <Card className="rounded-xl">
        <Row gutter={[24, 24]}>
          <Col xs={24} md={6} lg={6}>
            <Divider orientation="left" plain>
              {room ? "Sửa phòng chiếu" : "Tạo phòng chiếu"}
            </Divider>
            <Space direction="vertical" style={{ width: "100%" }} size="middle">
              <div style={{ marginBottom: "12px" }}>
                <label style={{ display: "block", marginBottom: "4px" }}>Tên Phòng chiếu:</label>
                <Input
                  value={name}
                  onChange={(e) => setName(e.target.value)}
                  placeholder="Tên phòng"
                />
                {error && (
                  <div style={{ color: "red", fontSize: "13px", marginTop: "2px" }}>{error}</div>
                )}
              </div>
              <Row gutter={12}>
                <Col span={12}>
                  Số hàng:
                  <Input
                    type="number"
                    value={rows}
                    onChange={(e) => setRows(Math.min(+e.target.value, 15))}
                    placeholder="Số hàng (tối đa 15)"
                  />
                </Col>
                <Col span={12}>
                  Số cột:
                  <Input
                    type="number"
                    value={cols}
                    onChange={(e) => setCols(Math.min(+e.target.value, 15))}
                    placeholder="Số cột (tối đa 15)"
                  /> 
                </Col>
              </Row>
              <Button type="primary" onClick={generateSeats} block>
                Tạo sơ đồ ghế
              </Button>
              <Space>
                <Button type="primary" onClick={handleSubmit} icon={<SquarePen size={16} />}>
                  Lưu
                </Button>
                <Button danger onClick={onBack} icon={<X size={16} />}>
                  Hủy
                </Button>
              </Space>
            </Space>
          </Col>
          <Col xs={24} md={18} lg={18}>
            <Divider orientation="left" plain>
              Sơ đồ ghế
            </Divider>
            <div style={{ overflowX: "auto", paddingBottom: 12 }}>
              <div
                style={{
                  borderRadius: "10px",
                  padding: "10px",
                  margin: "0 auto 20px",
                  color: "#fff",
                  backgroundColor: "#1677FF",
                  width: `${cols * 42 + (cols - 1) * 6}px`,
                  minWidth: 300,
                  textAlign: "center",
                  fontWeight: 600,
                  boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
                }}
              >
                Màn hình
              </div>
              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: `repeat(${cols}, 40px)`,
                  gap: 6,
                  justifyContent: "center",
                  minWidth: `${cols * 40 + (cols - 1) * 6}px`,
                }}
              >
                {Object.keys(seatTypes).map((code) => (
                  <Tooltip
                    key={code}
                    title={`Ghế ${code} (${["Thường", "VIP", "Đôi"][seatTypes[code]]})`}
                  >
                    <div
                      onClick={() => toggleSeatType(code)}
                      style={{
                        backgroundColor: seatColors[seatTypes[code]],
                        width: 40,
                        height: 40,
                        borderRadius: 6,
                        display: "flex",
                        alignItems: "center",
                        justifyContent: "center",
                        cursor: "pointer",
                        boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                        fontWeight: 500,
                        fontSize: 12,
                      }}
                    >
                      {code}
                    </div>
                  </Tooltip>
                ))}
              </div>
              <Space style={{ marginTop: 16 }}>
                <Tag color="#e0e0e0">Thường</Tag>
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
