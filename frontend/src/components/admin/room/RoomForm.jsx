// import React, { useState, useEffect } from "react";
// import axios from "axios";
// import { Input, Button, Card, Row, Col, message, Divider, Badge } from "antd";

// const blue = "rgb(22, 119, 255)";
// const seatColors = ["#e0e0e0", "#f74551", "#f536db"]; // regular, vip, couple

// export default function RoomForm({ room, onBack }) {
//   const [name, setName] = useState("");
//   const [rows, setRows] = useState(0);
//   const [cols, setCols] = useState(0);
//   const [seatTypes, setSeatTypes] = useState({});
//   const [error, setError] = useState("");

//   useEffect(() => {
//     if (room) {
//       axios.get(`http://localhost:8081/api/public/rooms/${room.id}`)
//         .then(res => {
//           const data = res.data;
//           const rowCount = data.rows;
//           const colCount = data.cols;
//           const seatsFromAPI = {};

//           data.seats.forEach(seat => {
//             const rowChar = seat.seatRow;
//             const colNumber = parseInt(seat.seatCol) + 1;
//             const code = rowChar + colNumber;
//             const typeName = seat.seatType?.seatTypeName?.toLowerCase() || "regular";

//             seatsFromAPI[code] =
//               typeName === "vip" ? 1 :
//               typeName === "couple" ? 2 : 0;
//           });

//           const completeSeats = {};
//           for (let r = 0; r < rowCount; r++) {
//             for (let c = 0; c < colCount; c++) {
//               const code = String.fromCharCode(65 + r) + (c + 1);
//               completeSeats[code] = seatsFromAPI[code] ?? (r < 4 ? 0 : r === rowCount - 1 ? 2 : 1);
//             }
//           }

//           setName(data.name);
//           setRows(rowCount);
//           setCols(colCount);
//           setSeatTypes(completeSeats);
//         })
//         .catch(err => {
//           console.error("Lỗi khi load phòng:", err);
//           message.error("Không thể tải dữ liệu phòng.");
//         });
//     }
//   }, [room]);

//   const generateSeats = () => {
//     const seats = {};
//     for (let r = 0; r < rows; r++) {
//       for (let c = 0; c < cols; c++) {
//         const code = String.fromCharCode(65 + r) + (c + 1);
//         seats[code] = r < 4 ? 0 : r === rows - 1 ? 2 : 1;
//       }
//     }
//     setSeatTypes(seats);
//   };

//   const toggleSeatType = (code) => {
//     setSeatTypes(prev => ({
//       ...prev,
//       [code]: (prev[code] + 1) % 3,
//     }));
//   };

//   const validateName = async () => {
//     if (!name.trim()) {
//       setError("Tên phòng không được để trống.");
//       return false;
//     }

//     if (!room) {
//       try {
//         const res = await axios.get("http://localhost:8081/api/public/rooms");
//         const existing = res.data.map(r => r.cinemaRoomName.toLowerCase());
//         if (existing.includes(name.trim().toLowerCase())) {
//           setError("Tên phòng đã tồn tại.");
//           return false;
//         }
//       } catch (err) {
//         message.error("Không thể kiểm tra tên phòng.");
//         return false;
//       }
//     }

//     setError("");
//     return true;
//   };

//   const handleSubmit = async () => {
//     const isValid = await validateName();
//     if (!isValid) return;

//     const seats = Object.keys(seatTypes).map(code => ({
//       seatRow: code.charCodeAt(0) - 65,
//       seatCol: parseInt(code.slice(1)) - 1,
//       seatType: seatTypes[code] === 0 ? "regular" : seatTypes[code] === 1 ? "vip" : "couple"
//     }));

//     const payload = { name, rows, cols, seats };

//     try {
//       room
//         ? await axios.put(`http://localhost:8081/api/public/rooms/${room.id}`, payload)
//         : await axios.post("http://localhost:8081/api/public/rooms", payload);

//       message.success("Lưu phòng thành công");
//       onBack();
//     } catch (err) {
//       console.error("Lỗi khi lưu:", err);
//       message.error("Lỗi khi lưu phòng.");
//     }
//   };

//   return (
//     <div className="p-4 bg-white min-h-screen">
//       <Row gutter={24}>
//         <Col xs={24} lg={10}>
//           <Card title={room ? "Sửa phòng chiếu" : "Tạo phòng chiếu"} className="rounded-2xl shadow-sm">
//             <label className="fw-semibold">Tên phòng</label>
//             <Input
//               value={name}
//               onChange={e => setName(e.target.value)}
//               placeholder="Nhập tên phòng"
//             />
//             {error && <div className="text-danger mt-1">{error}</div>}

//             <Row gutter={16} className="mt-3">
//               <Col span={12}>
//                 <label className="fw-semibold">Số hàng</label>
//                 <Input
//                   type="number"
//                   min={1}
//                   max={15}
//                   value={rows}
//                   onChange={e => setRows(Math.min(+e.target.value, 15))}
//                 />
//               </Col>
//               <Col span={12}>
//                 <label className="fw-semibold">Số cột</label>
//                 <Input
//                   type="number"
//                   min={1}
//                   max={15}
//                   value={cols}
//                   onChange={e => setCols(Math.min(+e.target.value, 15))}
//                 />
//               </Col>
//             </Row>

//             <Button type="primary" className="mt-3 w-100 fw-bold" onClick={generateSeats}>
//               Tạo sơ đồ ghế
//             </Button>

//             <Divider />

//             <Row justify="space-between">
//               <Col>
//                 <Button type="primary" className="fw-bold" onClick={handleSubmit}>
//                   Lưu
//                 </Button>
//               </Col>
//               <Col>
//                 <Button className="fw-bold" onClick={onBack}>
//                   Hủy
//                 </Button>
//               </Col>
//             </Row>
//           </Card>
//         </Col>

//         <Col xs={24} lg={14}>
//           <Card title="Sơ đồ ghế" className="rounded-2xl shadow-sm">
//             <div className="text-center mb-3 fw-bold" style={{ backgroundColor: blue, color: "#fff", padding: "6px", borderRadius: "8px" }}>
//               Màn hình
//             </div>

//             <div
//               className="d-grid gap-2 justify-content-center"
//               style={{
//                 gridTemplateColumns: `repeat(${cols}, 40px)`,
//                 display: "grid",
//                 justifyContent: "center"
//               }}
//             >
//               {Object.keys(seatTypes).map((code) => (
//                 <div
//                   key={code}
//                   onClick={() => toggleSeatType(code)}
//                   className="text-center fw-semibold"
//                   style={{
//                     backgroundColor: seatColors[seatTypes[code]],
//                     width: "40px",
//                     height: "40px",
//                     lineHeight: "40px",
//                     cursor: "pointer",
//                     borderRadius: "6px",
//                     border: "1px solid #ccc",
//                     boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
//                   }}
//                 >
//                   {code}
//                 </div>
//               ))}
//             </div>

//             <div className="mt-4 d-flex gap-3">
//               <Badge color={seatColors[0]} text="Thường" />
//               <Badge color={seatColors[1]} text="VIP" />
//               <Badge color={seatColors[2]} text="Đôi" />
//             </div>
//           </Card>
//         </Col>
//       </Row>
//     </div>
//   );
// }
import React, { useState, useEffect } from "react";
import axios from "axios";
import { Input, Button, Card, Row, Col, message, Divider, Tooltip, Space, Tag } from "antd";
import {SquarePen, X} from "lucide-react"
const seatColors = ["#e0e0e0", "#f74551", "#f536db"]; // regular, vip, couple

export default function RoomForm({ room, onBack }) {
  const [name, setName] = useState("");
  const [rows, setRows] = useState(0);
  const [cols, setCols] = useState(0);
  const [seatTypes, setSeatTypes] = useState({});
  const [error, setError] = useState("");

  useEffect(() => {
    if (room) {
      axios.get(`http://localhost:8081/api/public/rooms/${room.id}`)
        .then(res => {
          const data = res.data;
          const rowCount = data.rows;
          const colCount = data.cols;
          const seatsFromAPI = {};

          data.seats.forEach(seat => {
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
        .catch(err => {
          console.error("Lỗi khi load phòng:", err);
          message.error("Không thể tải dữ liệu phòng.");
        });
    }
  }, [room]);

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
    setSeatTypes(prev => ({
      ...prev,
      [code]: (prev[code] + 1) % 3,
    }));
  };

  const validateName = async () => {
    if (!name.trim()) {
      setError("Tên phòng không được để trống.");
      return false;
    }

    if (!room) {
      try {
        const res = await axios.get("http://localhost:8081/api/public/rooms");
        const existing = res.data.map(r => r.cinemaRoomName.toLowerCase());
        if (existing.includes(name.trim().toLowerCase())) {
          setError("Tên phòng đã tồn tại.");
          return false;
        }
      } catch (err) {
        message.error("Không thể kiểm tra tên phòng.");
        return false;
      }
    }

    setError("");
    return true;
  };

  const handleSubmit = async () => {
    const isValid = await validateName();
    if (!isValid) return;

    const seats = Object.keys(seatTypes).map(code => ({
      seatRow: code.charCodeAt(0) - 65,
      seatCol: parseInt(code.slice(1)) - 1,
      seatType: seatTypes[code] === 0 ? "regular" : seatTypes[code] === 1 ? "vip" : "couple"
    }));

    const payload = { name, rows, cols, seats };

    try {
      room
        ? await axios.put(`http://localhost:8081/api/public/rooms/${room.id}`, payload)
        : await axios.post("http://localhost:8081/api/public/rooms", payload);

      message.success("Lưu phòng thành công");
      onBack();
    } catch (err) {
      console.error("Lỗi khi lưu:", err);
      message.error("Lỗi khi lưu phòng.");
    }
  };

  return (
    <div className="p-6 min-h-screen bg-[#f9fafb]">
      <Card className="shadow-md rounded-2xl border border-gray-200">
        <Row gutter={[32, 32]}>
          {/* Form nhập thông tin */}
          <Col xs={24} md={10}>
            <Divider orientation="left">{room ? "Sửa phòng chiếu" : "Tạo phòng chiếu"}</Divider>
            <Card className="shadow p-6 rounded-xl bg-white border border-gray-100">
              <label className="font-semibold text-sm text-gray-600">Tên phòng</label>
              <Input value={name} onChange={e => setName(e.target.value)} placeholder="Nhập tên phòng" />
              {error && <div className="text-red-500 mt-1">{error}</div>}

              <Row gutter={16} className="mt-3">
                <Col span={12}>
                  <label className="font-semibold text-sm text-gray-600">Số hàng</label>
                  <Input
                    type="number"
                    min={1}
                    max={15}
                    value={rows}
                    onChange={e => setRows(Math.min(+e.target.value, 15))}
                  />
                </Col>
                <Col span={12}>
                  <label className="font-semibold text-sm text-gray-600">Số cột</label>
                  <Input
                    type="number"
                    min={1}
                    max={15}
                    value={cols}
                    onChange={e => setCols(Math.min(+e.target.value, 15))}
                  />
                </Col>
              </Row>

              <Button type="primary" className="mt-4 w-full font-semibold" onClick={generateSeats}>
                Tạo sơ đồ ghế
              </Button>

              <Divider />

              <Space className="w-full justify-between">
                <Button type="primary" onClick={handleSubmit} className="font-semibold">
                  <SquarePen size={16} className="me-1 mt-1" />Cập nhật
                </Button>
                <Button onClick={onBack} className="font-semibold bg-secondary text-light">
                 <X className='me-1' /> Hủy
                </Button>
              </Space>
            </Card>
          </Col>

          {/* Sơ đồ ghế */}
          <Col xs={24} md={14}>
            <Divider orientation="left">Sơ đồ ghế</Divider>
            <Card className="p-4 bg-white rounded-xl shadow-sm border border-gray-100">
              <div
                className="text-center font-medium text-base mb-4"
                style={{
                  borderRadius: "10px",
                  padding: "10px",
                  margin: "0 auto",
                  color: "#fff",
                  backgroundColor: "#1677FF",
                  width: `${cols * 48 + (cols - 1) * 8}px`,
                  maxWidth: "100%",
                  boxShadow: "0 2px 6px rgba(0,0,0,0.1)",
                }}
              >
                Màn hình
              </div>

              <div
                style={{
                  display: "grid",
                  gridTemplateColumns: `repeat(${cols}, 40px)`,
                  gap: "8px",
                  justifyContent: "center",
                }}
              >
                {Object.keys(seatTypes).map(code => (
                  <Tooltip key={code} title={`Ghế ${code} (${["Thường", "VIP", "Đôi"][seatTypes[code]]})`}>
                    <div
                      className="hover:scale-105"
                      style={{
                        backgroundColor: seatColors[seatTypes[code]],
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
                      onClick={() => toggleSeatType(code)}
                    >
                      {code}
                    </div>
                  </Tooltip>
                ))}
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
