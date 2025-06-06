import { useEffect, useState } from "react";
import { Breadcrumb, Layout } from "antd";
import { Outlet, useLocation } from "react-router-dom";
import { AdminSidebar } from "../../components/admin/AdminSidebar";
import { AdminHeader } from "../../components/admin/AdminHeader";
import { AdminFooter } from "../../components/admin/AdminFooter";
import { fetchAllAccountAPI } from "../../service/AccountService";


export const AdminLayout = () => {
    const { Content } = Layout;
    const location = useLocation();

    const [collapsed, setCollapsed] = useState(false);
    const [dataUsers, setDataUsers] = useState(null);
    const [filter, setFilter] = useState("");
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(10);
    const [total, setTotal] = useState(0);
    const [breadcrumbItems, setBreadcrumbItems] = useState([]);
    const [refreshFlag, setRefreshFlag] = useState(false);


    useEffect(() => {
        if (location.pathname === '/admin') {
            setBreadcrumbItems([
                { title: 'Trang chủ' },
                { title: 'Bảng điều khiển' },
            ]);
        }
    }, [location.pathname]);

    useEffect(() => {
        const loadAccounts = async () => {
            const res = await fetchAllAccountAPI(page, size, filter);
            if (res && res.data) {
                setPage(res.data.meta.page);
                setSize(res.data.meta.pageSize);
                setTotal(res.data.meta.total);
                setDataUsers(res.data.result);
            }
        }

        loadAccounts();
    }, [page, filter, size, refreshFlag]);


    const toggleCollapsed = () => {
        setCollapsed(!collapsed);
    };

    return (
        <>
            <Layout style={{ minHeight: '100vh' }}>
                <AdminSidebar
                    collapsed={collapsed}
                    width={256}
                    theme={"light"}
                    setFilter={setFilter}
                />

                <Layout>
                    <AdminHeader
                        collapsed={collapsed}
                        toggleCollapsed={toggleCollapsed}
                    />

                    <div className="mx-4 mt-6">
                        <Breadcrumb
                            separator=">"
                            items={breadcrumbItems}
                        />
                    </div>

                    <Content style={{ margin: '24px 16px' }}>
                        {/* While color background for content */}
                        <div style={{
                            padding: 24,
                            // minHeight: 360,
                            background: '#fff',
                            borderRadius: '8px',
                            boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
                        }}
                        >
                            <Outlet
                                context={{
                                    dataUsers,
                                    page, setPage, size, setSize, total,
                                    setFilter, setBreadcrumbItems, setRefreshFlag
                                }}
                            />
                        </div>
                    </Content>

                    <AdminFooter />
                </Layout>
            </Layout>
        </>
    );
}
