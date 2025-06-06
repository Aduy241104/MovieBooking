import React, { useState, useEffect } from "react";
import axios from "axios";

export default function RoomForm({ room, onBack }) {
  const [name, setName] = useState("");
  const [rows, setRows] = useState(0);
  const [cols, setCols] = useState(0);
  const [seatTypes, setSeatTypes] = useState({});
  const [error, setError] = useState("");

  const seatColors = ["#2c2c2c", "#c62828", "#e91e63"]; // regular, vip, double

  useEffect(() => {
    if (room) {
      axios.get(`/api/rooms/${room.id}`)
        .then(res => {
          const data = res.data;
          const rowCount = data.rows;
          const colCount = data.cols;

          const seatsFromAPI = {};
          data.seats.forEach(seat => {
            const rowChar = seat.seatRow; // Đã là "A", "B", ...
            const colNumber = parseInt(seat.seatCol) + 1;
            const code = rowChar + colNumber;

            let typeName = "";
            if (seat.seatType && seat.seatType.seatTypeName) {
              typeName = seat.seatType.seatTypeName.toLowerCase();
            }

            seatsFromAPI[code] =
              typeName === "regular" ? 0 :
                typeName === "vip" ? 1 :
                  typeName === "double" ? 2 : 0;
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
        const res = await axios.get("/api/rooms");
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
          seatTypes[code] === 1 ? "vip" : "double"
    }));

    const payload = { name, rows, cols, seats };

    try {
      const res = room
        ? await axios.put(`/api/rooms/${room.id}`, payload)
        : await axios.post("/api/rooms", payload);

      console.log("Lưu thành công:", res.data);
      onBack();
    } catch (err) {
      console.error("Lỗi khi lưu:", err.response || err.message || err);
      alert("Lỗi khi lưu phòng.");
    }
  };

  return (
    <div className="container-fluid p-4 bg-dark text-white" style={{ minHeight: "100vh" }}>
      <div className="d-flex flex-column flex-lg-row gap-4">
        {/* Left Column: Info */}
        <div className="flex-grow-1 bg-black p-4 rounded-3 shadow">
          <h3 className="mb-4 border-bottom pb-2 text-danger text-nowrap">
            {room ? "Sửa phòng chiếu" : "Tạo phòng chiếu"}
          </h3>

          <div className="mb-3">
            <label className="form-label">Tên phòng</label>
            <input
              type="text"
              className="form-control bg-dark text-white border-secondary"
              value={name}
              onChange={e => setName(e.target.value)}
            />
            {error && <div className="text-danger mt-1">{error}</div>}
          </div>

          <div className="row mb-3">
            <div className="col">
              <label className="form-label">Số hàng</label>
              <input
                type="number"
                className="form-control bg-dark text-white border-secondary"
                value={rows}
                min={1}
                max={26}
                onChange={e => setRows(Math.min(+e.target.value, 26))}
              />
            </div>
            <div className="col">
              <label className="form-label">Số cột</label>
              <input
                type="number"
                className="form-control bg-dark text-white border-secondary"
                value={cols}
                min={1}
                max={20}
                onChange={e => setCols(Math.min(+e.target.value, 20))}
              />
            </div>
          </div>

          <button className="btn btn-danger mb-4" onClick={generateSeats}>
            Tạo sơ đồ ghế
          </button>

          <div className="d-flex gap-2">
            <button className="btn btn-danger" onClick={handleSubmit}>Lưu</button>
            <button className="btn btn-secondary" onClick={onBack}>Hủy</button>
          </div>
        </div>

        {/* Right Column: Seat layout */}
        <div className="flex-grow-2 bg-black p-4 rounded-3 shadow w-100">
          <div className="text-center text-white fw-bold py-2 mb-3 rounded bg-danger">
            Màn hình
          </div>

          <div
            className="d-grid gap-2 justify-content-center"
            style={{
              gridTemplateColumns: `repeat(${cols}, 40px)`,
              display: "grid",
              justifyContent: "center"
            }}
          >
            {Object.keys(seatTypes).map((code) => (
              <div
                key={code}
                className="text-center text-light rounded"
                style={{
                  width: "40px",
                  height: "40px",
                  backgroundColor: seatColors[seatTypes[code]],
                  cursor: "pointer",
                  lineHeight: "40px",
                  border: seatTypes[code] === 0 ? "2px solid white" : "none"
                }}
                onClick={() => toggleSeatType(code)}
              >
                {code}
              </div>
            ))}
          </div>

          <div className="mt-4">
            <span className="badge me-2" style={{ backgroundColor: "#2c2c2c" }}>Thường</span>
            <span className="badge me-2" style={{ backgroundColor: "#c62828" }}>VIP</span>
            <span className="badge" style={{ backgroundColor: "#e91e63" }}>Đôi</span>
          </div>
        </div>
      </div>
    </div>
  );
}