import React, { useState, useEffect } from "react";
import axios from "axios";
export default function RoomForm({ room, onBack }) {
  const [name, setName] = useState("");
  const [rows, setRows] = useState(0);
  const [cols, setCols] = useState(0);
  const [seatTypes, setSeatTypes] = useState({});
  const [error, setError] = useState("");
  const blue = "rgb(22, 119, 255)";
  const seatColors = ["#e0e0e0", "#f74551", "#f536db"]; // regular, vip, couple

  useEffect(() => {
    if (room) {
      axios.get(`http://localhost:8081/api/public/rooms/${room.id}`)
        .then(res => {
          const data= res.data;
          const rowCount = data.rows;
          const colCount = data.cols;

          const seatsFromAPI = {};
          data.seats.forEach(seat => {
            const rowChar = seat.seatRow;
            const colNumber = parseInt(seat.seatCol) + 1;
            const code = rowChar + colNumber;

            let typeName = "";
            if (seat.seatType && seat.seatType.seatTypeName) {
              typeName = seat.seatType.seatTypeName.toLowerCase();
            }

            seatsFromAPI[code] =
              typeName === "regular" ? 0 :
                typeName === "vip" ? 1 :
                  typeName === "couple" ? 2 : 0;
          });

          setName(data.name);
          setRows(rowCount);
          setCols(colCount);

          const completeSeats = {};
          for (let r = 0; r < rowCount; r++) {
            for (let c = 0; c < colCount; c++) {
              const code = String.fromCharCode(65 + r) + (c + 1);
              completeSeats[code] = seatsFromAPI[code] ?? (r < 4 ? 0 : r === rowCount - 1 ? 2 : 1);
            }
          }

          setSeatTypes(completeSeats);
        })
        .catch(err => {
          console.error("Lỗi khi load phòng:", err);
          alert("Không thể tải dữ liệu phòng.");
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
        console.error("Lỗi khi kiểm tra tên phòng:", err);
        setError("Không thể kiểm tra tên phòng.");
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
      seatType:
        seatTypes[code] === 0 ? "regular" :
          seatTypes[code] === 1 ? "vip" : "couple"
    }));

    const payload = { name, rows, cols, seats };

    try {
      const res = room
        ? await axios.put(`http://localhost:8081/api/public/rooms/${room.id}`, payload)
        : await axios.post("http://localhost:8081/api/public/rooms", payload);

      console.log("Lưu thành công:", res.data);
      onBack();
    } catch (err) {
      console.error("Lỗi khi lưu:", err.response || err.message || err);
      alert("Lỗi khi lưu phòng.");
    }
  };

  return (
    <div className="container-fluid py-5 px-4 bg-white min-vh-100">
      <div className="d-flex flex-column flex-lg-row gap-4">
        {/* Left: Form */}
        <div className="flex-grow-1 p-4 shadow rounded-4 bg-light">
          <h4 className="fw-bold border-bottom pb-3 mb-4" style={{ color: blue }}>
            {room ? "Sửa phòng chiếu" : "Tạo phòng chiếu"}
          </h4>

          <div className="mb-3">
            <label className="form-label fw-semibold" style={{ color: blue }}>Tên phòng</label>
            <input
              type="text"
              className="form-control border-secondary"
              value={name}
              onChange={e => setName(e.target.value)}
            />
            {error && <div className="text-danger mt-1">{error}</div>}
          </div>

          <div className="row mb-3">
            <div className="col">
              <label className="form-label fw-semibold" style={{ color: blue }}>Số hàng</label>
              <input
                type="number"
                className="form-control border-secondary"
                value={rows}
                min={1}
                max={15}
                onChange={e => setRows(Math.min(+e.target.value, 15))}
              />
            </div>
            <div className="col">
              <label className="form-label fw-semibold" style={{ color: blue }}>Số cột</label>
              <input
                type="number"
                className="form-control border-secondary"
                value={cols}
                min={1}
                max={15}
                onChange={e => setCols(Math.min(+e.target.value, 15))}
              />
            </div>
          </div>

          <button className="btn fw-bold text-white mb-4" style={{ backgroundColor: blue }} onClick={generateSeats}>
            Tạo sơ đồ ghế
          </button>

          <div className="d-flex gap-2">
            <button className="btn fw-bold text-white" style={{ backgroundColor: blue }} onClick={handleSubmit}>
              Lưu
            </button>
            <button className="btn btn-outline-secondary fw-bold" onClick={onBack}>
              Hủy
            </button>
          </div>
        </div>

        {/* Right: Seat layout */}
        <div className="flex-grow-2 p-4 shadow rounded-4 bg-light w-100">
          <div
            className="text-white text-center fw-bold py-2 rounded-3 mb-3"
            style={{ backgroundColor: blue }}
          >
            Màn hình
          </div>

          <div
            className="d-grid gap-2 justify-content-center"
            style={{
              gridTemplateColumns: `repeat(${cols}, 40px)`,
            }}
          >
            {Object.keys(seatTypes).map((code) => (
              <div
                key={code}
                className="rounded-2 text-dark text-center fw-semibold"
                style={{
                  backgroundColor: seatColors[seatTypes[code]],
                  width: "40px",
                  height: "40px",
                  lineHeight: "40px",
                  cursor: "pointer",
                  border: "1px solid #ccc",
                  boxShadow: "0 1px 3px rgba(0,0,0,0.1)",
                }}
                onClick={() => toggleSeatType(code)}
              >
                {code}
              </div>
            ))}
          </div>

          <div className="mt-4">
            <span className="badge rounded-pill me-2" style={{ backgroundColor: seatColors[0], color: "#000" }}>Thường</span>
            <span className="badge rounded-pill me-2" style={{ backgroundColor: seatColors[1], color: "#fff" }}>VIP</span>
            <span className="badge rounded-pill" style={{ backgroundColor: seatColors[2], color: "#fff" }}>Đôi</span>
          </div>
        </div>
      </div>
    </div>
  );
}
