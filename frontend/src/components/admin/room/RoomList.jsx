import React, { useEffect, useState } from "react";
import axios from "axios";
import { useNavigate } from "react-router-dom";
import 'bootstrap/dist/css/bootstrap.min.css';
import {SquarePlus, Projector} from "lucide-react";
export default function RoomList() {
  const [rooms, setRooms] = useState([]);
  const navigate = useNavigate();

  const fetchRooms = async () => {
    try {
      const res = await axios.get("/api/public/rooms");
      setRooms(res.data);
    } catch (err) {
      console.error("Lỗi khi load danh sách phòng:", err);
      alert("Không thể tải danh sách phòng.");
    }
  };

  useEffect(() => {
    fetchRooms();
  }, []);

  return (
    <div className="container py-5" style={{ backgroundColor: "#ffffff", minHeight: "100vh" }}>
      <div className="d-flex justify-content-between align-items-center mb-4">
        <h2 className="d-flex" style={{ color: "#1677ff", fontWeight: "bold", fontSize: "2rem" }}>
          <Projector size={35} strokeWidth={1.75} style={{marginRight:"5px"}} /> Danh sách phòng chiếu
        </h2>
        <button
          className="btn d-flex"
          style={{
            backgroundColor: "#1677ff",
            color: "#ffffff",
            fontWeight: "bold",
            fontSize: "1.1rem",
            padding: "10px 20px",
            boxShadow: "0 4px 12px rgba(22, 119, 255, 0.3)"
          }}
          onClick={() => navigate("/admin/room-list/add-room")}
        >
          <SquarePlus strokeWidth={1.75} style={{marginRight:"5px"}}/> Thêm phòng
        </button>
      </div>

      {rooms.length === 0 ? (
        <p className="text-muted fs-5">Không có phòng chiếu nào.</p>
      ) : (
        <ul className="list-group list-group-flush">
          {rooms.map((room, index) => (
            <li
              key={room.cinemaRoomId}
              className="list-group-item d-flex justify-content-between align-items-center py-3 px-4"
              style={{
                borderColor: "#dee2e6",
                cursor: "pointer",
                backgroundColor: "#f9f9f9",
                fontSize: "20px"
              }}
              onClick={() => navigate(`/admin/room-list/room/${room.cinemaRoomId}`)}
            >
              <span style={{ color: "#1677ff", fontWeight: "500" }}>
                {index + 1}. {room.cinemaRoomName}
              </span>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
}
