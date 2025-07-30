import { useEffect, useState } from "react";
import { Breadcrumb, Layout, message } from "antd";
import { Outlet, useLocation } from "react-router-dom";
import { AdminSidebar } from "../../components/admin/AdminSidebar";
import { AdminHeader } from "../../components/admin/AdminHeader";
import { AdminFooter } from "../../components/admin/AdminFooter";
import { fetchAllAccountAPI } from "../../service/AccountService";
import "../../components/admin/admin.scss";

export const AdminLayout = () => {
    const { Content } = Layout;
    const location = useLocation();

    const [collapsed, setCollapsed] = useState(false);
    const [dataUsers, setDataUsers] = useState([]);
    const [filter, setFilter] = useState("");
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(10);
    const [total, setTotal] = useState(0);
    const [breadcrumbItems, setBreadcrumbItems] = useState([]);
    const [refreshFlag, setRefreshFlag] = useState(false);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (location.pathname === "/admin") {
            setBreadcrumbItems([{ title: "Trang chủ" }, { title: "Bảng điều khiển" }]);
        }
    }, [location.pathname]);

    useEffect(() => {
        setIsLoading(true);
        const loadAccounts = async () => {
            if (location.pathname.includes("users-")) {
                try {
                    const res = await fetchAllAccountAPI(page, size, filter);
                    if (res && res.result) {
                        setTotal(res.result.meta.total);
                        setDataUsers(res.result.data);
                    }
                    setIsLoading(false);
                } catch (error) {
                    if (error.message === "Network Error") {
                        message.error("Không thể kết nối tới máy chủ. Vui lòng kiểm tra lại kết nối hoặc thử lại sau!");
                        setDataUsers([]);
                        setTotal(0);
                    } else {
                        message.error(`Đã xảy ra lỗi: ${error.message}. Vui lòng thử lại sau!`);
                    }
                }
            }
        };

        loadAccounts();
    }, [page, filter, size, refreshFlag]);

    const toggleCollapsed = () => {
        setCollapsed(!collapsed);
    };

    return (
        <>
            <Layout style={{ minHeight: "100vh" }}>
                <AdminSidebar collapsed={collapsed} width={256} theme={"dark"} />

                <Layout>
                    <AdminHeader collapsed={collapsed} toggleCollapsed={toggleCollapsed} />

                    <div className="mx-4 mt-6">
                        <Breadcrumb separator=">" items={breadcrumbItems} />
                    </div>

                    <Content style={{ margin: "24px 16px" }}>
                        <Outlet
                            context={{
                                dataUsers,
                                isLoading,
                                setIsLoading,
                                page,
                                setPage,
                                size,
                                total,
                                setFilter,
                                setBreadcrumbItems,
                                setRefreshFlag,
                            }}
                        />
                    </Content>

                    <AdminFooter />
                </Layout>
            </Layout>
        </>
    );
};
