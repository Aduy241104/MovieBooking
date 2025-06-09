import React, { useEffect, useState } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";

export default function RoomDetail() {
    const { id: roomId } = useParams();
    const navigate = useNavigate();
    const [room, setRoom] = useState(null);

    const seatColors = {
        regular: "#2c2c2c",
        vip:"#c62828",     // xám sangs
        double: "#e91e63",   // tím đỏ sang
    };

    useEffect(() => {
        const fetchRoom = async () => {
            try {
                const res = await axios.get(`/api/public/rooms/${roomId}`);
                setRoom(res.data);
            } catch (err) {
                console.error("Lỗi khi lấy chi tiết phòng:", err);
            }
        };

        fetchRoom();
    }, [roomId]);

    const handleDelete = async () => {
        if (window.confirm("Bạn có chắc muốn xóa phòng này?")) {
            try {
                await axios.delete(`/api/public/rooms/${roomId}`);
                navigate("/admin/room-list");
            } catch (err) {
                console.error("Lỗi khi xóa phòng:", err);
                alert("Không thể xóa phòng.");
            }
        }
    };

    if (!room) return <div className="text-light p-5">Đang tải...</div>;

    return (
        <div className="container vh-100 vw-100 text-light  d-flex flex-column ">
            <div className="container-fluid bg-dark rounded-4 shadow-lg p-4 flex-grow-1">
                <div className="row h-100">
                    {/* Cột trái */}
                    <div className="col-md-5 d-flex flex-column justify-content-between pe-4">
                        <div>
                            <h2 className="text-danger fw-bold mb-4">{room.name}</h2>
                            <div className="mb-4">
                                <p className="mb-2"><strong>Số hàng:</strong> {room.rows}</p>
                                <p className="mb-2"><strong>Số cột:</strong> {room.cols}</p>
                                <p className="mb-0"><strong>Tổng ghế:</strong> {room.seats.length}</p>
                            </div>

                            <h6 className="mt-4 mb-2">Loại ghế:</h6>
                            <div className="mb-4">
                                <span className="badge px-3 py-2 me-2" style={{ backgroundColor: seatColors.regular }}>Thường</span>
                                <span className="badge px-3 py-2 me-2" style={{ backgroundColor: seatColors.vip }}>VIP</span>
                                <span className="badge px-3 py-2" style={{ backgroundColor: seatColors.double }}>Đôi</span>
                            </div>
                        </div>

                        <div className="d-grid gap-2">
                            <button className="btn btn-outline-light rounded-3 fw-bold" onClick={() => navigate(`/admin/room-list/${roomId}/edit`)}>
                                ✏️ Sửa thông tin                                                                
                            </button>
                            <button className="btn btn-danger rounded-3 fw-bold" onClick={handleDelete}>
                                🗑️ Xóa phòng
                            </button>

                            <button className="btn btn-secondary rounded-3 fw-bold" onClick={() => navigate("/admin/room-list")}>
                                🔙 Quay lại danh sách
                            </button>
                        </div>
                    </div>

                    {/* Cột phải */}
                    <div className="col-md-7 d-flex align-items-center justify-content-center">
                        <div className="d-flex flex-column align-items-center justify-content-center">
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
                                            className="rounded-2 text-white text-center fw-semibold"
                                            style={{
                                                backgroundColor: seatColors[type],
                                                aspectRatio: "1",
                                                display: "flex",
                                                alignItems: "center",
                                                justifyContent: "center",
                                                fontSize: "0.9rem",
                                                border: "1px solid #444",
                                                boxShadow: "0 2px 4px rgba(0,0,0,0.4)",
                                            }}
                                        >
                                            {seat.seatRow + (parseInt(seat.seatCol) + 1)}
                                        </div>
                                    );
                                })}
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}