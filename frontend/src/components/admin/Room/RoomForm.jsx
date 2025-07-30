import React, { useState, useEffect } from "react";
import axios from "axios";
import {
  Input,
  Button,
  Card,
  Row,
  Col,
  Divider,
  Space,
  Tag,
  notification,
} from "antd";
import { SquarePen, X } from "lucide-react";

// Define seat type names and colors (index: 0 = regular, 1 = vip, 2 = couple)
const seatTypeNames = ["Thường", "VIP", "Đôi"];
const seatColors = ["#e0e0e0", "#f74551", "#f536db"];

export default function RoomForm({ room, onBack }) {
  const [roomName, setRoomName] = useState("");
  const [rowCount, setRowCount] = useState(0);
  const [colCount, setColCount] = useState(0);
  const [seatMap, setSeatMap] = useState({});
  const [errorMsg, setErrorMsg] = useState("");
  const token = localStorage.getItem("token");

  useEffect(() => {
    if (!room) return;

    const fetchRoomDetails = async () => {
      try {
        const res = await axios.get(`http://localhost:8081/api/rooms/${room.id}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        const { name, rows, cols, seats } = res.data;
        const mappedSeats = {};

        seats.forEach(({ seatRow, seatCol, seatType }) => {
          const code = `${seatRow}${parseInt(seatCol) + 1}`;
          const type = seatType?.seatTypeName?.toLowerCase() || "regular";
          mappedSeats[code] = type === "vip" ? 1 : type === "couple" ? 2 : 0;
        });

        const completeMap = {};
        for (let r = 0; r < rows; r++) {
          for (let c = 0; c < cols; c++) {
            const code = `${String.fromCharCode(65 + r)}${c + 1}`;
            completeMap[code] = mappedSeats[code] ?? (r < 4 ? 0 : r === rows - 1 ? 2 : 1);
          }
        }

        setRoomName(name);
        setRowCount(rows);
        setColCount(cols);
        setSeatMap(completeMap);
      } catch (err) {
        console.error("Lỗi khi tải phòng:", err);
        notification.error({
          message: "TẢI DỮ LIỆU THẤT BẠI",
          description: "Không thể tải dữ liệu phòng.",
        });
      }
    };

    fetchRoomDetails();
  }, [room, token]);

  const generateSeats = () => {
    const newMap = {};
    for (let r = 0; r < rowCount; r++) {
      for (let c = 0; c < colCount; c++) {
        const code = `${String.fromCharCode(65 + r)}${c + 1}`;
        newMap[code] = r < 4 ? 0 : r === rowCount - 1 ? 2 : 1;
      }
    }
    setSeatMap(newMap);
  };

  const toggleSeatType = (code) => {
    setSeatMap((prev) => ({ ...prev, [code]: (prev[code] + 1) % seatTypeNames.length }));
  };

  const validateRoomName = async () => {
    if (!roomName.trim()) {
      setErrorMsg("Tên phòng không được để trống.");
      return false;
    }

    try {
      const res = await axios.get("http://localhost:8081/api/rooms", {
        headers: { Authorization: `Bearer ${token}` },
      });

      const isDuplicate = res.data.some(
        (r) =>
          r.cinemaRoomName &&
          r.cinemaRoomName.toLowerCase() === roomName.trim().toLowerCase() &&
          (!room || r.cinemaRoomId !== room.id)
      );

      if (isDuplicate) {
        setErrorMsg("Tên phòng đã tồn tại.");
        return false;
      }
    } catch (err) {
      console.error("Lỗi kiểm tra tên phòng:", err.response?.data || err.message);
      notification.error({
        message: "KIỂM TRA TÊN PHÒNG THẤT BẠI",
        description: err.response?.data?.error || "Không thể kiểm tra tên phòng.",
      });
      return false;
    }

    setErrorMsg("");
    return true;
  };

  const handleSubmit = async () => {
    const valid = await validateRoomName();
    if (!valid) return;

    const seats = Object.entries(seatMap).map(([code, type]) => ({
      seatRow: code.charCodeAt(0) - 65,
      seatCol: parseInt(code.slice(1)) - 1,
      seatType: ["regular", "vip", "couple"][type],
    }));

    const payload = {
      name: roomName,
      rows: rowCount,
      cols: colCount,
      seats,
    };

    try {
      if (room) {
        await axios.put(`http://localhost:8081/api/rooms/${room.id}`, payload, {
          headers: { Authorization: `Bearer ${token}` },
        });
        notification.success({
          message: "CẬP NHẬT THÀNH CÔNG",
          description: "Cập nhật phòng chiếu thành công.",
        });
      } else {
        await axios.post("http://localhost:8081/api/rooms", payload, {
          headers: { Authorization: `Bearer ${token}` },
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

  const renderSeatGrid = () => (
    <>
      <div
        style={{
          borderRadius: 10,
          padding: 10,
          margin: "0 auto 20px",
          color: "#fff",
          backgroundColor: "#1677FF",
          width: `${colCount * 42 + (colCount - 1) * 6}px`,
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
          gridTemplateColumns: `repeat(${colCount}, 40px)`,
          gap: 6,
          justifyContent: "center",
          minWidth: `${colCount * 40 + (colCount - 1) * 6}px`,
        }}
      >
        {Object.entries(seatMap).map(([code, type]) => (
          <div
            key={code}
            onClick={() => toggleSeatType(code)}
            style={{
              backgroundColor: seatColors[type],
              width: 40,
              height: 40,
              borderRadius: 6,
              display: "flex",
              alignItems: "center",
              justifyContent: "center",
              cursor: "pointer",
              fontWeight: 500,
              fontSize: 12,
              boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
            }}
          >
            {code}
          </div>
        ))}
      </div>
      <Space style={{ marginTop: 16 }}>
        {seatTypeNames.map((name, index) => (
          <Tag key={index} color={seatColors[index]}>
            {name}
          </Tag>
        ))}
      </Space>
    </>
  );

  return (
    <div className="p-6 min-h-screen bg-white">
      <Card className="rounded-xl">
        <Row gutter={[24, 24]}>
          <Col xs={24} md={6}>
            <Divider orientation="left" plain>
              {room ? "Sửa phòng chiếu" : "Tạo phòng chiếu"}
            </Divider>
            <Space direction="vertical" style={{ width: "100%" }} size="middle">
              <div>
                <label style={{ display: "block", marginBottom: 4 }}>Tên Phòng chiếu:</label>
                <Input
                  value={roomName}
                  onChange={(e) => setRoomName(e.target.value)}
                  placeholder="Tên phòng"
                />
                {errorMsg && (
                  <div style={{ color: "red", fontSize: 13, marginTop: 2 }}>{errorMsg}</div>
                )}
              </div>
              <Row gutter={12}>
                <Col span={12}>
                  <label>Số hàng:</label>
                  <Input
                    type="number"
                    min={0}
                    value={rowCount}
                    onChange={(e) =>
                      setRowCount(Math.max(0, Math.min(+e.target.value, 15)))
                    }
                    placeholder="0 - 15"
                  />
                </Col>
                <Col span={12}>
                  <label>Số cột:</label>
                  <Input
                    type="number"
                    min={0}
                    value={colCount}
                    onChange={(e) =>
                      setColCount(Math.max(0, Math.min(+e.target.value, 15)))
                    }
                    placeholder="0 - 15"
                  />
                </Col>
              </Row>
              <Button type="primary" block onClick={generateSeats}>
                Tạo sơ đồ ghế
              </Button>
              <Space>
                <Button type="primary" icon={<SquarePen size={16} />} onClick={handleSubmit}>
                  Lưu
                </Button>
                <Button danger icon={<X size={16} />} onClick={onBack}>
                  Hủy
                </Button>
              </Space>
            </Space>
          </Col>
          <Col xs={24} md={18}>
            <Divider orientation="left" plain>
              Sơ đồ ghế
            </Divider>
            <div style={{ overflowX: "auto", paddingBottom: 12 }}>{renderSeatGrid()}</div>
          </Col>
        </Row>
      </Card>
    </div>
  );
}
