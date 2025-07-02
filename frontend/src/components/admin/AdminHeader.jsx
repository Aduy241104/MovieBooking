import { LogoutOutlined, MenuFoldOutlined, MenuUnfoldOutlined, UserOutlined } from "@ant-design/icons";
import { Avatar, Button, Dropdown } from "antd";
import { Header } from "antd/es/layout/layout";
import { useContext, useEffect, useState } from "react";
import { useLocation } from "react-router-dom";
import { AuthContext } from "../../context/AuthContext";


export const AdminHeader = (props) => {
    const { collapsed, toggleCollapsed } = props;
    const location = useLocation();
    const [title, setTitle] = useState("");
    const { logout, user } = useContext(AuthContext);

    // console.log("AdminHeader rendered", user);

    useEffect(() => {
        if (location.pathname === '/admin') {
            setTitle("BẢNG ĐIỀU KHIỂN (CHỨC NĂNG ĐANG PHÁT TRIỂN)");
        } else if (location.pathname.includes('users-members')) {
            setTitle("QUẢN LÝ THÀNH VIÊN");
        } else if (location.pathname.includes('users-employees')) {
            setTitle("QUẢN LÝ NHÂN VIÊN");
        } else if (location.pathname.includes('promotions')) {
            setTitle("QUẢN LÝ MÃ GIẢM GIÁ");
        }else if (location.pathname.includes('room-list')) {
            setTitle("QUẢN LÝ PHÒNG CHIẾU");
        }else if (location.pathname.includes('movie-list')) {
            setTitle("QUẢN LÝ PHIM");
        }else if (location.pathname.includes('faretype-list')) {
            setTitle("QUẢN LÝ LOẠI VÉ");
        } else if (location.pathname.includes('booking-list')) {
            setTitle("QUẢN LÝ lỊCH SỬ ĐẶT VÉ");
        }

         else if (location.pathname.includes('activity-logs')) {
            setTitle("LỊCH SỬ HOẠT ĐỘNG");
        } else if (location.pathname.includes('movie-type')) {
            setTitle("DANH SÁCH THỂ LOẠI PHIM");
        } 
    }, [location.pathname]);

    const items = [
        {
            key: '1',
            type: 'group',
            label:
                <>
                    <div className="flex flex-col">
                        <p className="text-black">{user?.email}</p>
                        <p className="text-gray-500">Administrator</p>
                    </div>
                </>,
        },
        {
            type: 'divider',
        },
        // {
        //     key: '2',
        //     label:
        //         <>
        //             <div className="flex gap-3">
        //                 <UserOutlined />
        //                 <p>Tài khoản</p>
        //             </div>
        //         </>,
        // },
        {
            key: '3',
            label:
                <>
                    <div className="flex gap-3" onClick={logout}>
                        <LogoutOutlined />
                        <p>Đăng xuất</p>
                    </div>
                </>,
        }
    ];

    return (
        <>
            <Header
                style={{
                    padding: '0 24px',
                    background: '#fff',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'space-between',
                    boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                    zIndex: 1,
                }}
            >
                <div style={{ display: 'flex', alignItems: 'center' }}>
                    {/* Nút toggle navbar */}
                    <Button
                        type="text"
                        icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
                        onClick={toggleCollapsed}
                        style={{
                            fontSize: '16px',
                            width: 40,
                            height: 40,
                            marginRight: '16px',
                        }}
                    />
                    <h6 style={{ margin: 0, color: '#333', fontWeight: '500' }}>
                        {title}
                    </h6>
                </div>

                {/* Phần bên phải của Header */}
                <div style={{ display: 'flex', alignItems: 'center' }}>
                    <span style={{
                        color: '#666',
                        marginRight: '12px',
                        fontSize: '14px'
                    }}>
                        Chào mừng Admin!
                    </span>

                    {/* Có thể thêm avatar, notification, logout button ở đây */}

                    <div>
                        <Dropdown
                            menu={{ items }}
                            placement="bottomRight"
                        >
                            <Avatar size="default" icon={<UserOutlined />} />
                        </Dropdown>
                    </div>
                </div>
            </Header>
        </>
    );
}