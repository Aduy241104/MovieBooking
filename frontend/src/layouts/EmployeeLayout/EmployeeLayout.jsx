import { useEffect, useState } from "react";
import { Breadcrumb, Layout } from "antd";
import { Outlet, useLocation } from "react-router-dom";
import { EmployeeSidebar } from "../../components/employee/EmployeeSidebar";
import { EmployeeHeader } from "../../components/employee/EmployeeHeader";
import { EmployeeFooter } from "../../components/employee/EmployeeFooter";
import "../../components/employee/employee.scss";

export const EmployeeLayout = () => {
    const { Content } = Layout;
    const location = useLocation();

    const [collapsed, setCollapsed] = useState(false);
    const [filter, setFilter] = useState("");
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(10);
    const [total, setTotal] = useState(0);
    const [breadcrumbItems, setBreadcrumbItems] = useState([]);
    const [isLoading, setIsLoading] = useState(false);

    useEffect(() => {
        if (location.pathname === "/employee") {
            setBreadcrumbItems([{ title: "Trang chủ" }, { title: "Bảng điều khiển" }]);
        }
    }, [location.pathname]);

    const toggleCollapsed = () => {
        setCollapsed(!collapsed);
    };

    return (
        <>
            <Layout style={{ minHeight: "100vh" }}>
                <EmployeeSidebar collapsed={collapsed} width={256} theme={"light"} />

                <Layout>
                    <EmployeeHeader collapsed={collapsed} toggleCollapsed={toggleCollapsed} />

                    <div className="mx-4 mt-6">
                        <Breadcrumb separator=">" items={breadcrumbItems} />
                    </div>

                    <Content style={{ margin: "24px 16px" }}>
                        <Outlet
                            context={{
                                isLoading,
                                setIsLoading,
                                page,
                                setPage,
                                size,
                                total,
                                setFilter,
                                setBreadcrumbItems,
                            }}
                        />
                    </Content>

                    <EmployeeFooter />
                </Layout>
            </Layout>
        </>
    );
};
