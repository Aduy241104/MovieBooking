import React from "react";
import { useNavigate } from "react-router-dom";
import RoomForm from "./RoomForm";

export default function CreateRoom() {
  const navigate = useNavigate();

  const handleBack = () => {
    navigate("/admin/room-list");
  };

  return (
    <div className="">
      <RoomForm onBack={handleBack} isEdit={false} />
    </div>
  );
}