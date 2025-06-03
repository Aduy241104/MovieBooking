import { Layout } from "antd";
import { Users } from 'lucide-react'
import { Menu } from 'antd'
import { Link } from 'react-router-dom';
import { sfEqual } from 'spring-filter-query-builder';
import './admin.scss'


export const AdminSidebar = (props) => {
    const { collapsed, width, theme, setFilter, isSiderCollapsed } = props;
    const { Sider } = Layout;

    const handleFilterMember = () => {
        const filter = sfEqual('role.roleName', 'Member');
        setFilter(filter.toString());
    }


    return (
        <>
            <Sider
                trigger={null} // Tắt trigger mặc định của Sider
                collapsible
                collapsed={collapsed}
                theme={theme}
                width={width}
                style={{
                    boxShadow: '2px 0 8px 0 rgba(29,35,41,.05)',
                }}
            >
                <div
                    style={{
                        height: '64px', // Cùng chiều cao với Header
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: collapsed ? 'center' : 'flex-start',
                        padding: collapsed ? '0' : '0 24px',
                        borderBottom: '1px solid #f0f0f0',
                        background: '#fff',
                        transition: 'all 0.2s',
                    }}
                >
                    <div
                        style={{
                            width: collapsed ? '32px' : 'auto',
                            height: '32px',
                            background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
                            borderRadius: '8px',
                            display: 'flex',
                            alignItems: 'center',
                            justifyContent: 'center',
                            color: '#fff',
                            fontWeight: 'bold',
                            fontSize: collapsed ? '14px' : '16px',
                            marginRight: collapsed ? '0' : '12px',
                            transition: 'all 0.2s',
                        }}
                    >
                        {collapsed ? 'A' : 'AD'}
                    </div>
                    {!collapsed && (
                        <span
                            style={{
                                color: '#333',
                                fontWeight: '600',
                                fontSize: '16px',
                                whiteSpace: 'nowrap',
                            }}
                        >
                            Admin Dashboard
                        </span>
                    )}
                </div>

                <Menu mode="inline" theme="light"
                    className="menu-link"
                // defaultSelectedKeys={['1']}
                // defaultOpenKeys={['sub1']}
                >
                    <Menu.SubMenu
                        key="users"
                        icon={<Users size={20} strokeWidth={1.5} />}
                        title="Người dùng"
                    >
                        <Menu.Item key="members">
                            <Link to="users-members" onClick={handleFilterMember}>Thành viên</Link>
                        </Menu.Item>
                        <Menu.Item key="employees">
                            <Link to="users-employees">Nhân viên</Link>
                        </Menu.Item>
                    </Menu.SubMenu>
                </Menu>

            </Sider>
        </>
    );
}