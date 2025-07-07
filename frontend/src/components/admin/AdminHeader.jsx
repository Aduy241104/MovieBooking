import { LogoutOutlined, MenuFoldOutlined, MenuUnfoldOutlined, UserOutlined } from "@ant-design/icons";
import { Avatar, Button, Dropdown } from "antd";
import { Header } from "antd/es/layout/layout";
import { useContext, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";

export const AdminHeader = ({ collapsed, toggleCollapsed }) => {
    const location = useLocation();
    const [title, setTitle] = useState("");
    const { logout, user } = useContext(AuthContext);

    useEffect(() => {
        const path = location.pathname;
        if (path === "/admin") {
            setTitle("BẢNG ĐIỀU KHIỂN");
        } else if (path.includes("users-members")) {
            setTitle("QUẢN LÝ THÀNH VIÊN");
        } else if (path.includes("users-employees")) {
            setTitle("QUẢN LÝ NHÂN VIÊN");
        } else if (path.includes("promotions")) {
            setTitle("QUẢN LÝ MÃ GIẢM GIÁ");
        } else if (path.includes("room-list")) {
            setTitle("QUẢN LÝ PHÒNG CHIẾU");
        } else if (path.includes("movie-list") || path.includes("movies")) {
            setTitle("QUẢN LÝ PHIM");
        } else if (path.includes("movie-type")) {
            setTitle("QUẢN LÝ THỂ LOẠI PHIM");
        } else if (path.includes("faretype-list")) {
            setTitle("QUẢN LÝ LOẠI VÉ");
        } else if (path.includes("booking-list")) {
            setTitle("QUẢN LÝ LỊCH SỬ ĐẶT VÉ");
        } else if (path.includes("review-list")) {
            setTitle("QUẢN LÝ BÌNH LUẬN");
        } else if (path.includes("activity-logs")) {
            setTitle("LỊCH SỬ HOẠT ĐỘNG");
        } else {
            setTitle("");
        }
    }, [location.pathname]);

    const items = [
        {
            key: "1",
            type: "group",
            label: (
                <div className="flex flex-col">
                    <p className="text-black">{user?.email}</p>
                    <p className="text-gray-500">Administrator</p>
                </div>
            ),
        },
        { type: "divider" },
        {
            key: "3",
            label: (
                <div className="flex gap-3" onClick={logout}>
                    <LogoutOutlined />
                    <p>Đăng xuất</p>
                </div>
            ),
        },
    ];

    return (
        <Header
            style={{
                padding: "0 24px",
                background: "#fff",
                display: "flex",
                alignItems: "center",
                justifyContent: "space-between",
                boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
                zIndex: 1,
            }}
        >
            {/* Bên trái - Toggle & Title */}
            <div style={{ display: "flex", alignItems: "center" }}>
                <Button
                    type="text"
                    icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                    onClick={toggleCollapsed}
                    style={{
                        fontSize: "16px",
                        width: 40,
                        height: 40,
                        marginRight: "16px",
                    }}
                />
                <h6 style={{ margin: 0, color: "#333", fontWeight: "500" }}>{title}</h6>
            </div>

            {/* Bên phải - Avatar + Chào */}
            <div style={{ display: "flex", alignItems: "center" }}>
                <span style={{ color: "#666", marginRight: "12px", fontSize: "14px" }}>
                    Chào mừng Admin!
                </span>
                <Dropdown menu={{ items }} placement="bottomRight">
                    <Avatar size="default" icon={<UserOutlined />} />
                </Dropdown>
            </div>
        </Header>
    );
};
