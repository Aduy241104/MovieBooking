import { Layout, Menu } from "antd";
import {
  LayoutDashboard,
  Film,
  MessageSquareText,
  Receipt,
  History,
  CalendarDays, 
} from "lucide-react";
import { Link, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import "./employee.scss";

export const EmployeeSidebar = ({ collapsed, width, theme }) => {
  const { Sider } = Layout;
  const location = useLocation();
  const [openKeys, setOpenKeys] = useState([]);

  const items = [
     {
      key: "movie",
      icon: <Film size={20} strokeWidth={1.5} />,
      label: <Link to="/employee/movies">Danh sách phim</Link>,
    },
    {   
      key: "showtime-list",
      icon: <CalendarDays size={20} strokeWidth={1.5} />,
      label: <Link to="/employee/showtime-list">Quản lý lịch chiếu</Link>,
    },
    {
      key: "faretype",
      icon: <Receipt size={20} strokeWidth={1.5} />,
      label: "Quản lý giá vé",
      children: [
        {
          key: "faretype-list",
          label: <Link to="/employee/faretype-list">Loại giá vé</Link>,
        },
        {
          key: "booking-list",
          label: <Link to="/employee/booking-list">Lịch sử đặt vé</Link>,
        },
      ],
    },
    {
      key: "review-list",
      icon: <MessageSquareText size={20} strokeWidth={1.5} />,
      label: <Link to="/employee/review-list">Quản lý bình luận</Link>,
    },
    {
      key: "activity-logs",
      icon: <History size={20} strokeWidth={1.5} />,
      label: <Link to="/employee/activity-logs">Lịch sử hoạt động</Link>,
    },
  ];

  // Xác định selectedKeys dựa trên pathname
  const getSelectedKeys = () => {
    const pathname = location.pathname;
    if (pathname.includes("movies")) return ["movie"];
    if (pathname.includes("showtime-list")) return ["showtime-list"];
    if (pathname.includes("faretype-list")) return ["faretype-list"];
    if (pathname.includes("booking-list")) return ["booking-list"];
    if (pathname.includes("review-list")) return ["review-list"];
    if (pathname.includes("activity-logs")) return ["activity-logs"];
    return [];
  };

  // Cập nhật openKeys khi pathname thay đổi
  useEffect(() => {
    const pathname = location.pathname;
    if (pathname.includes("faretype-list") || pathname.includes("booking-list")) {
      setOpenKeys(["faretype"]);
    }
  }, [location.pathname]);

  // Đóng mở menu
  const handleOpenChange = (keys) => {
    setOpenKeys(keys);
  };

  return (
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
          // background: "#fff",
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
              color: theme === "dark" ? "#fff" : "#000",
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
        openKeys={openKeys}
        onOpenChange={handleOpenChange}
        mode="inline"
        theme="dark"
        inlineCollapsed={collapsed}
        items={items}
      />
    </Sider>
  );
};