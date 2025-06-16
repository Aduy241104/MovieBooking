import React, { useEffect, useState } from "react";
import axios from "axios";
import RoomForm from "./RoomForm";
import RoomDetail from "./RoomDetail";
import 'bootstrap/dist/css/bootstrap.min.css';
import { useNavigate } from "react-router-dom";
export default function RoomList() {
    const [rooms, setRooms] = useState([]);
    const [showForm, setShowForm] = useState(false);
    const [selectedRoomId, setSelectedRoomId] = useState(null);
    const navigate = useNavigate();
    const fetchRooms = async () => {
        const res = await axios.get(`${process.env.REACT_APP_BASE_URL}/public/rooms`);
        setRooms(res.data);
    };

    useEffect(() => {
        fetchRooms();
    }, []);

    const handleCloseForm = () => {
        setShowForm(false);
        fetchRooms();
    };
    return (
        <div className="py-4 ps-2 pe-2 bg-dark text-light min-vh-100">
            {!selectedRoomId ? (
                <>
                    <h2 className="mb-4 text-danger">Danh sách phòng chiếu</h2>
                    <button
                        className="btn btn-danger mb-4"
                        // onClick={() => setShowForm(true)}
                        onClick={() => navigate("/admin/room-list/add-room")}
                    >
                        Thêm phòng chiếu
                    </button>

                    <div className="row g-3">
                        {rooms.map((room) => (
                            <div className="col-md-6 col-lg-4" key={room.cinemaRoomId}>
                                <div
                                    className="card bg-white text-light h-100 shadow"
                                    style={{ cursor: "pointer" }}
                                    // onClick={() => setSelectedRoomId(room.cinemaRoomId)}
                                    onClick={() => navigate(`/admin/room-list/room/${room.cinemaRoomId}`)}
                                >
                                    <div className="card-body">
                                        <h5 className="card-title text-danger">{room.cinemaRoomName}</h5>
                                    </div>
                                </div>
                            </div>
                        ))}
                    </div>
                </>
            ) : (
                <RoomDetail roomId={selectedRoomId} onClose={() => {
                    setSelectedRoomId(null);
                    fetchRooms();
                }} />
            )}

            {showForm && (
                <div
                    className="modal show d-block"
                    tabIndex="-1"
                    style={{ backgroundColor: "rgba(218, 214, 214, 0.8)" }}
                >
                    <div className="modal-dialog modal-lg modal-dialog-centered">
                        <div className="modal-content bg-dark text-light">
                            <div className="modal-header border-secondary">
                                <h5 className="modal-title text-danger">➕ Thêm phòng chiếu</h5>
                                <button
                                    type="button"
                                    className="btn-close btn-close-white"
                                    onClick={handleCloseForm}
                                ></button>
                            </div>
                            <div className="modal-body">
                                <RoomForm onBack={handleCloseForm} />
                            </div>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
}