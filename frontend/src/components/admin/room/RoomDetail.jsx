import React, { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";
import axios from "axios";
import {
    Pencil,
    Trash,
    ArrowLeft
} from "lucide-react";

export default function RoomDetail() {
    const { id: roomId } = useParams();
    const navigate = useNavigate();
    const [room, setRoom] = useState(null);

    const blue = "rgb(22, 119, 255)";
    const seatColors = {
        regular: "#e0e0e0",
        vip: "#f74551",
        couple: "#f536db",
    };

    useEffect(() => {
        const fetchRoom = async () => {
            try {
                const data = await axios.get(`http://localhost:8081/api/public/rooms/${roomId}`);
                setRoom(data.data);
            } catch (err) {
                console.error("Lỗi khi lấy chi tiết phòng:", err);
            }
        };
        fetchRoom();
    }, [roomId]);

    const handleDelete = async () => {
        if (window.confirm("Bạn có chắc muốn xóa phòng này?")) {
            try {
                await axios.delete(`http://localhost:8081/api/public/rooms/${roomId}`);
                navigate("/admin/room-list");
            } catch (err) {
                console.error("Lỗi khi xóa phòng:", err);
                alert("Không thể xóa phòng.");
            }
        }
    };

    if (!room) return <div className="text-center p-5" style={{ color: blue }}>Đang tải...</div>;

    return (
        <div className="container-fluid min-vh-100 bg-white py-5 px-4">
            <div className="bg-light rounded-4 shadow p-4">
                <div className="row">
                    {/* Thông tin phòng */}
                    <div className="col-md-4 d-flex flex-column justify-content-between border-end pe-4">
                        <div >
                            <h3 style={{ color: blue,fontSize:"50px" }} className="fw-bold mb-4">{room.name}</h3>
                            <p><strong style={{ color: blue,fontSize:"18px" }}>Số hàng:</strong> {room.rows}</p>
                            <p><strong style={{ color: blue,fontSize:"18px" }}>Số cột:</strong> {room.cols}</p>
                            <p><strong style={{ color: blue,fontSize:"18px" }}>Tổng ghế:</strong> {room.seats.length}</p>
                        </div>

                        <div className="d-grid gap-3">
                            <button
                                className="btn border border-primary text-primary d-flex align-items-center gap-2 fw-bold"
                                onClick={() => navigate(`/admin/room-list/${roomId}/edit`)}
                                style={{ color: blue, borderColor: blue }}
                            >
                                <Pencil size={18} style={{marginRight:"3px"}}/> Sửa thông tin
                            </button>

                            <button
                                className="btn btn-outline-danger d-flex align-items-center gap-2 fw-bold"
                                onClick={handleDelete}
                            >
                                <Trash size={18} style={{marginRight:"3px"}}/> Xóa phòng
                            </button>

                            <button
                                className="btn btn-outline-secondary d-flex align-items-center gap-2 fw-bold"
                                onClick={() => navigate("/admin/room-list")}
                            >
                                <ArrowLeft size={18} style={{marginRight:"3px"}}/> Quay lại
                            </button>
                        </div>
                    </div>

                    {/* Sơ đồ ghế */}
                    <div className="col-md-8 mt-4 mt-md-0 d-flex flex-column align-items-center">
                        <div
                            className="text-white text-center py-2 px-5 rounded-3 mb-4 fw-bold"
                            style={{ backgroundColor: blue, width:"60%" }}
                        >
                            Màn hình
                        </div>
                        <div>
                             
                        </div>
                        <div
                            style={{
                                display: "grid",
                                gridTemplateColumns: `repeat(${room.cols}, 40px)`,
                                gap: "8px",
                            }}
                        >
                            {room.seats.map((seat) => {
                                const type = seat.seatType?.seatTypeName?.toLowerCase() || "regular";
                                return (
                                    <div
                                        key={seat.seatCol + seat.seatRow}
                                        className="rounded-2 text-dark text-center fw-semibold"
                                        style={{
                                            backgroundColor: seatColors[type],
                                            aspectRatio: "1",
                                            display: "flex",
                                            alignItems: "center",
                                            justifyContent: "center",
                                            fontSize: "0.9rem",
                                            border: "1px solid #ccc",
                                            boxShadow: "0 2px 4px rgba(0,0,0,0.1)",
                                        }}
                                    >
                                        {seat.seatRow + (parseInt(seat.seatCol) + 1)}
                                    </div>
                                );
                            })}
                        </div>

                        {/* Chú thích loại ghế */}
                        <h6 className="mt-4 mb-2 fw-bold" style={{ color: blue }}>Loại ghế:</h6>
                        <div className="mb-4">
                            <span className="badge rounded-pill me-2" style={{ backgroundColor: seatColors.regular, color: "#000" }}>Thường</span>
                            <span className="badge rounded-pill me-2" style={{ backgroundColor: seatColors.vip, color: "#fff" }}>VIP</span>
                            <span className="badge rounded-pill" style={{ backgroundColor: seatColors.couple, color: "#fff" }}>Đôi</span>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}