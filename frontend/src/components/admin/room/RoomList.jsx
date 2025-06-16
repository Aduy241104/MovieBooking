import React, { useEffect, useState, useCallback } from "react";

import { useNavigate } from "react-router-dom";
import { SquarePlus } from "lucide-react";
import { Table, Input, message } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import "bootstrap/dist/css/bootstrap.min.css";
import instance from "../../../config/axios";
const RoomList = () => {
    const [rooms, setRooms] = useState([]);
    const [filteredRooms, setFilteredRooms] = useState([]);
    const [searchValue, setSearchValue] = useState("");
    const navigate = useNavigate();
    const fetchRooms = useCallback(async () => {
        try {
            const data = await instance.get("/public/rooms");
            setRooms(data);
            setFilteredRooms(data);
        } catch (err) {
            console.error("Lỗi khi load danh sách phòng:", err);
            message.error("Không thể tải danh sách phòng.");
        }
    }, []);

    useEffect(() => {
        fetchRooms();
    }, [fetchRooms]);

    const handleSearch = (value) => {
        setSearchValue(value);
        const filtered = rooms.filter(room =>
            room.cinemaRoomName.toLowerCase().includes(value.toLowerCase())
        );
        setFilteredRooms(filtered);
    };

    const columns = [
        {
            title: "STT",
            render: (_, __, index) => <>{index + 1}</>,
            width: 80,
        },
        {
            title: "Tên phòng chiếu",
            dataIndex: "cinemaRoomName",
            render: (text) => (
                <span className="text-primary fw-semibold">{text}</span>
            ),
        }
    ];

    return (
        <div className="p-4" style={{ backgroundColor: "#ffffff", minHeight: "100vh" }}>
            <div className="d-flex justify-content-between align-items-center mb-4">
                <div style={{ width: "300px" }}>
                    <Input
                        placeholder="Tìm theo tên phòng..."
                        allowClear
                        addonAfter={<SearchOutlined />}
                        value={searchValue}
                        onChange={(e) => handleSearch(e.target.value)}
                        style={{ width: "30vw" }}
                    />
                </div>
                <button
                    className="btn d-flex align-items-center"
                    style={{
                        backgroundColor: "#1677ff",
                        color: "#ffffff",
                        fontWeight: "bold",
                        fontSize: "1.05rem",
                        padding: "8px 16px",
                        boxShadow: "0 4px 12px rgba(22, 119, 255, 0.3)",
                        borderRadius: "6px"
                    }}
                    onClick={() => navigate("/admin/room-list/add-room")}
                >
                    <SquarePlus strokeWidth={1.7} className="me-2" /> Thêm phòng
                </button>
            </div>

            <Table
                columns={columns}
                dataSource={filteredRooms}
                rowKey="cinemaRoomId"
                pagination={false}
                onRow={(record) => ({
                    onClick: () => navigate(`/admin/room-list/room/${record.cinemaRoomId}`),
                })}
                rowClassName="table-row-hover"
                style={{ border: "1px solid #f0f0f0", borderRadius: "8px" }}
            />
        </div>
    );
};

export default RoomList;
