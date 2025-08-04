import React, { useEffect } from "react";
import { useNavigate, useLocation, useOutletContext } from "react-router-dom";
import RoomForm from "./RoomForm";
export default function CreateRoom() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();

  useEffect(() => {
    if (location.pathname === '/admin/room-list/add-room') {
      setBreadcrumbItems([
        { title: 'Trang chủ'  },
        { title: 'Phòng chiếu' },
        { title: 'Thêm phòng chiếu' },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);
  const handleBack = () => {
    navigate("/admin/room-list");
  };

  return (
    <div className="">
      <RoomForm onBack={handleBack} isEdit={false} />
    </div>
  );
}