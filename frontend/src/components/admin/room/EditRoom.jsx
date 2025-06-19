import React, { useEffect, useState } from "react";
import { useNavigate, useParams,useLocation, useOutletContext } from "react-router-dom";
import RoomForm from "./RoomForm";
import axios from "axios";
export default function EditRoom() {
  const { id } = useParams();
  const [room, setRoom] = useState(null);
  const navigate = useNavigate();
const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();
  useEffect(() => {
    if (location.pathname.includes('/admin/room-list/room/edit')) {
      setBreadcrumbItems([
        { title: 'Trang chủ' },
        { title: 'Phòng chiếu' },
        { title: 'Chi tiết phòng chiếu' },
        { title: 'Chỉnh sửa phòng chiếu' },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);
  useEffect(() => {
    axios.get(`http://localhost:8081/api/public/rooms/${id}`)
      .then(res => setRoom(res.data))
      .catch(err => {
        console.error("Không thể tải phòng:", err);
        alert("Không thể tải phòng.");
        navigate("/admin/room-list");
      });
  }, [id, navigate]);

  const handleBack = () => {
    navigate(`/admin/room-list/room/${id}`);
  };

  return (
    <div className="">
      {room ? (
        <RoomForm room={room} onBack={handleBack} isEdit />
      ) : (
        <p>Đang tải...</p>
      )}
    </div>
  );
}