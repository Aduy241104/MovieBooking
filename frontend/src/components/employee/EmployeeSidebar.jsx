import { Layout } from "antd";
import { LayoutDashboard, Users } from "lucide-react";
import { Menu } from "antd";
import { Link, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import "./employee.scss";

export const EmployeeSidebar = (props) => {
    const { collapsed, width, theme } = props;
    const { Sider } = Layout;
    const location = useLocation();
    const [openKeys, setOpenKeys] = useState([]);

    const items = [
        {
            key: "dashboard",
            icon: <LayoutDashboard size={20} strokeWidth={1.5} />,
            label: <Link to={"/employee"}>Dashboard</Link>,
        },
    ];

    // Xác định selectedKeys dựa trên pathname
    const getSelectedKeys = () => {
        const pathname = location.pathname;
        if (pathname === "/employee") {
            return ["dashboard"];
        }
        return [];
    };

    // Cập nhật openKeys khi pathname thay đổi
    useEffect(() => {
        const pathname = location.pathname;
        if (pathname.includes("users-members") || pathname.includes("users-employees")) {
            setOpenKeys(["users"]);
        } else {
            setOpenKeys([]);
        }
        // Không reset openKeys khi ở dashboard để menu vẫn có thể mở được
    }, [location.pathname]);

    // Đóng mở menu (Người dùng)
    const handleOpenChange = (keys) => {
        // console.log('Open keys changed:', keys);
        setOpenKeys(keys);
    };

    return (
        <>
            <Sider
                trigger={null}
                collapsible
                collapsed={collapsed}
                theme={theme}
                width={width}
                style={{
                    boxShadow: "2px 0 8px 0 rgba(29,35,41,.05)",
                }}
            >
                <div
                    style={{
                        height: "64px",
                        display: "flex",
                        alignItems: "center",
                        justifyContent: collapsed ? "center" : "flex-start",
                        padding: collapsed ? "0" : "0 24px",
                        borderBottom: "1px solid #f0f0f0",
                        background: "#fff",
                        transition: "all 0.2s",
                    }}
                >
                    <div
                        style={{
                            width: collapsed ? "32px" : "auto",
                            height: "32px",
                            background: "linear-gradient(135deg, #667eea 0%, #764ba2 100%)",
                            borderRadius: "8px",
                            display: "flex",
                            alignItems: "center",
                            justifyContent: "center",
                            color: "#fff",
                            fontWeight: "bold",
                            fontSize: collapsed ? "14px" : "16px",
                            marginRight: collapsed ? "0" : "12px",
                            transition: "all 0.2s",
                        }}
                    >
                        {collapsed ? "E" : "ED"}
                    </div>
                    {!collapsed && (
                        <span
                            style={{
                                color: "#333",
                                fontWeight: "600",
                                fontSize: "16px",
                                whiteSpace: "nowrap",
                            }}
                        >
                            Employee Dashboard
                        </span>
                    )}
                </div>

                <Menu
                    selectedKeys={getSelectedKeys()}
                    openKeys={collapsed ? [] : openKeys} // Sử dụng state openKeys
                    onOpenChange={handleOpenChange} // Đóng mở menu và cập nhật openKeys
                    mode="inline"
                    theme="light"
                    inlineCollapsed={collapsed}
                    items={items}
                />
            </Sider>
        </>
    );
};
