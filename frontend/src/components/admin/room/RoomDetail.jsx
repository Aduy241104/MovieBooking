// import React, { useEffect, useState } from "react";
// import axios from "axios";
// import { useParams, useNavigate } from "react-router-dom";
// import * as LucideIcons from 'lucide-react';



// export default function RoomDetail() {
//     const { id: roomId } = useParams();
//     const navigate = useNavigate();
//     const [room, setRoom] = useState(null);

//     const seatColors = {
//         regular: "#2c2c2c",     // xám sang
//         vip: "#c62828",      // đỏ đô
//         couple: "#e91e63",   // tím đỏ sang
//     };

//     useEffect(() => {
//         const fetchRoom = async () => {
//             try {
//                 const res = await axios.get(`/api/public/rooms/${roomId}`);
//                 setRoom(res.data);
//             } catch (err) {
//                 console.error("Lỗi khi lấy chi tiết phòng:", err);
//             }
//         };

//         fetchRoom();
//     }, [roomId]);

//     const handleDelete = async () => {
//         if (window.confirm("Bạn có chắc muốn xóa phòng này?")) {
//             try {
//                 await axios.delete(`/api/public/rooms/${roomId}`);
//                 navigate("/admin/room-list");
//             } catch (err) {
//                 console.error("Lỗi khi xóa phòng:", err);
//                 alert("Không thể xóa phòng.");
//             }
//         }
//     };

//     if (!room) return <div className="text-light p-5">Đang tải...</div>;

//     return (
//         <div className="container vh-100 vw-100  d-flex flex-column ">
//             <div className="container-fluid  rounded-4 shadow-lg p-4 flex-grow-1">
//                 <div className="row h-100">
//                     {/* Cột trái */}
//                     <div className="col-md-5 d-flex flex-column justify-content-between pe-4">
//                         <div>
//                             <h2 className="text-danger fw-bold mb-4">{room.name}</h2>
//                             <div className="mb-4">
//                                 <p className="mb-2"><strong>Số hàng:</strong> {room.rows}</p>
//                                 <p className="mb-2"><strong>Số cột:</strong> {room.cols}</p>
//                                 <p className="mb-0"><strong>Tổng ghế:</strong> {room.seats.length}</p>
//                             </div>

                            
//                         </div>

//                         <div className="d-grid gap-2">
//                             <button className="btn btn-dark rounded-3 fw-bold d-flex w-50" onClick={() => navigate(`/admin/room-list/${roomId}/edit`)}>
//                                 <LucideIcons.Pencil strokeWidth={1.75} />Sửa thông tin
//                             </button>
//                             <button className="btn btn-danger rounded-3 fw-bold d-flex w-50" onClick={handleDelete}>
//                                 <LucideIcons.Trash strokeWidth={1.75} />Xóa phòng
//                             </button>

//                             <button className="btn btn-secondary w-50 rounded-3 fw-bold d-flex center" onClick={() => navigate("/admin/room-list")}>
//                                 <LucideIcons.ArrowLeft /> Quay lại danh sách
//                             </button>
//                         </div>
//                     </div>

//                     {/* Cột phải */}
//                     <div className="col-md-7 d-flex align-items-center justify-content-center">

//                         <div className="d-flex flex-column align-items-center justify-content-center">
//                             <div className="text-center text-white fw-bold py-2 mb-3 rounded bg-danger w-100">
//                                 Màn hình
//                             </div>
//                             <div
//                                 style={{
//                                     display: "grid",
//                                     gridTemplateColumns: `repeat(${room.cols}, 40px)`,
//                                     gap: "8px",
//                                 }}
//                             >
//                                 {room.seats.map((seat) => {
//                                     const type = seat.seatType?.seatTypeName?.toLowerCase() || "regular";
//                                     return (
//                                         <div
//                                             key={seat.seatCol + seat.seatRow}
//                                             className="rounded-2 text-white text-center fw-semibold"
//                                             style={{
//                                                 backgroundColor: seatColors[type],
//                                                 aspectRatio: "1",
//                                                 display: "flex",
//                                                 alignItems: "center",
//                                                 justifyContent: "center",
//                                                 fontSize: "0.9rem",
//                                                 border: "1px solid #444",
//                                                 boxShadow: "0 2px 4px rgba(0,0,0,0.4)",
//                                             }}
//                                         >
//                                             {seat.seatRow + (parseInt(seat.seatCol) + 1)}
//                                         </div>
//                                     );
//                                 })}
//                             </div>
//                             <h6 className="mt-4 mb-2">Loại ghế:</h6>
//                             <div className="mb-4">
//                                 <span className="badge px-3 py-2 me-2" style={{ backgroundColor: seatColors.regular }}>Thường</span>
//                                 <span className="badge px-3 py-2 me-2" style={{ backgroundColor: seatColors.vip }}>VIP</span>
//                                 <span className="badge px-3 py-2" style={{ backgroundColor: seatColors.couple }}>Đôi</span>
//                             </div>
//                         </div>
//                     </div>
//                 </div>
//             </div>
//         </div>
//     );
// }

import React, { useEffect, useState } from "react";
import axios from "axios";
import { useParams, useNavigate } from "react-router-dom";
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
