import { Layout, Menu } from "antd";
import {
  LayoutDashboard,
  Ticket,
  Users,
  Film,
  Video,
  CalendarDays,
  Receipt,
  MessageSquareText,
  History,
} from "lucide-react";
import { Link, useLocation } from "react-router-dom";
import { useState, useEffect } from "react";
import "./admin.scss";

export const AdminSidebar = ({ collapsed, width, theme }) => {
  const { Sider } = Layout;
  const location = useLocation();
  const [openKeys, setOpenKeys] = useState([]);

  const items = [
    {
      key: "dashboard",
      icon: <LayoutDashboard size={20} strokeWidth={1.5} />,
      label: <Link to="/admin">Dashboard</Link>,
    },
    {
      key: "users",
      icon: <Users size={20} strokeWidth={1.5} />,
      label: "Người dùng",
      children: [
        {
          key: "members",
          label: <Link to="/admin/users-members">Thành viên</Link>,
        },
        {
          key: "employees",
          label: <Link to="/admin/users-employees">Nhân viên</Link>,
        },
      ],
    },
    {
      key: "promotions",
      icon: <Ticket size={20} strokeWidth={1.5} />,
      label: <Link to="/admin/promotions">Mã khuyến mãi</Link>,
    },
    {
      key: "room-list",
      icon: <Video size={20} strokeWidth={1.5} />,
      label: <Link to="/admin/room-list">Danh sách phòng chiếu</Link>,
    },
    {
      key: "movie",
      icon: <Film size={20} strokeWidth={1.5} />,
      label: "Quản lý phim",
      children: [
        {
          key: "movies",
          label: <Link to="/admin/movies">Phim</Link>,
        },
        {
          key: "movie-type",
          label: <Link to="/admin/movie-type">Thể loại</Link>,
        },
      ],
    },
    {
      key: "showtime-list",
      icon: <CalendarDays size={20} strokeWidth={1.5} />,
      label: <Link to="/admin/showtime-list">Quản lý lịch chiếu</Link>,
    },
    {
      key: "faretype",
      icon: <Receipt size={20} strokeWidth={1.5} />,
      label: "Quản lý giá vé",
      children: [
        {
          key: "faretype-list",
          label: <Link to="/admin/faretype-list">Loại giá vé</Link>,
        },
        {
          key: "booking-list",
          label: <Link to="/admin/booking-list">Lịch sử đặt vé</Link>,
        },
      ],
    },
    {
      key: "review-list",
      icon: <MessageSquareText size={20} strokeWidth={1.5} />,
      label: <Link to="/admin/review-list">Quản lý bình luận</Link>,
    },
    {
      key: "activity-logs",
      icon: <History size={20} strokeWidth={1.5} />,
      label: <Link to="/admin/activity-logs">Lịch sử hoạt động</Link>,
    },
  ];

  const getSelectedKeys = () => {
    const path = location.pathname;
    if (path === "/admin") return ["dashboard"];
    if (path.includes("users-members")) return ["members"];
    if (path.includes("users-employees")) return ["employees"];
    if (path.includes("promotions")) return ["promotions"];
    if (path.includes("room-list")) return ["room-list"];
    if (path.includes("movies")) return ["movies"];
    if (path.includes("movie-type")) return ["movie-type"];
    if (path.includes("showtime-list")) return ["showtime-list"];
    if (path.includes("faretype-list")) return ["faretype-list"];
    if (path.includes("booking-list")) return ["booking-list"];
    if (path.includes("review-list")) return ["review-list"];
    if (path.includes("activity-logs")) return ["activity-logs"];
    return [];
  };

  useEffect(() => {
    const path = location.pathname;
    if (path.includes("users-members") || path.includes("users-employees")) {
      setOpenKeys(["users"]);
    } else if (path.includes("movies") || path.includes("movie-type")) {
      setOpenKeys(["movie"]);
    } else if (path.includes("faretype-list") || path.includes("booking-list")) {
      setOpenKeys(["faretype"]);
    } else {
      setOpenKeys([]);
    }
  }, [location.pathname]);

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
          {collapsed ? "A" : "AD"}
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
            Admin Dashboard
          </span>
        )}
      </div>

      <Menu
        selectedKeys={getSelectedKeys()}
        openKeys={collapsed ? [] : openKeys}
        onOpenChange={handleOpenChange}
        mode="inline"
        theme="light"
        inlineCollapsed={collapsed}
        items={items}
      />
    </Sider>
  );
};
