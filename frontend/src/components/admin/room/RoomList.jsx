import React, { useEffect, useState, useCallback } from "react";
import { useLocation, useNavigate, useOutletContext } from "react-router-dom";
import { SquarePlus } from "lucide-react";
import { Table, Input, Button, Card, message } from "antd";
import { SearchOutlined } from "@ant-design/icons";
import axios from "axios";

const RoomList = () => {
    const [rooms, setRooms] = useState([]);
    const [filteredRooms, setFilteredRooms] = useState([]);
    const [searchValue, setSearchValue] = useState("");

    const navigate = useNavigate();
    const location = useLocation();
    const { setBreadcrumbItems } = useOutletContext();

    useEffect(() => {
        if (location.pathname === '/admin/room-list') {
            setBreadcrumbItems([
                { title: 'Trang chủ' },
                { title: 'Phòng chiếu' },
            ]);
        }
    }, [location.pathname, setBreadcrumbItems]);
    const fetchRooms = useCallback(async () => {
        try {
            const res = await axios.get("http://localhost:8081/api/public/rooms");
            console.log("Dữ liệu phòng:", res.data); // kiểm tra seats có tồn tại không
            setRooms(res.data);
            setFilteredRooms(res.data);
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
            render: (text, record) => (
                <span
                    className=" cursor-pointer hover:underline"
                    onClick={() => navigate(`/admin/room-list/room/${record.cinemaRoomId}`)}
                >
                    {text}
                </span>
            ),
        },

    ];

    return (
        <div className="p-4 bg-white min-h-screen  shadow-sm" style={{borderRadius:"16px"}}>
            <Card className="">
                <div className="d-flex justify-content-between align-items-center mb-4">
                   
                    <Input style={{ width: "30vw" }}
                            size='large'
                            addonBefore={<SearchOutlined />}
                            placeholder="Tìm kiếm tài khoản..."
                            allowClear
                            value={searchValue}
                            onChange={(value) => handleSearch(value.target.value)}
                        />
                    <Button
                        type="primary"
                        icon={<SquarePlus size={18} strokeWidth={1.7} />}
                        onClick={() => navigate("/admin/room-list/add-room")}
                        style={{ fontWeight: "bold", padding: "6px 20px" }}
                    >
                        Thêm phòng
                    </Button>
                </div>

                <Table
                    columns={columns}
                    dataSource={filteredRooms}
                    rowKey="cinemaRoomId"
                    pagination={false}
                    rowClassName="hover:bg-gray-50 cursor-pointer"
                    onRow={(record) => ({
                        onClick: () => navigate(`/admin/room-list/room/${record.cinemaRoomId}`),
                    })}
                />
            </Card>
        </div>
    );
};

export default RoomList;