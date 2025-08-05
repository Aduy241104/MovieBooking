import { SearchOutlined } from "@ant-design/icons";
import { Button, Input, Select } from "antd";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { TicketPlus } from "lucide-react";
import { debounce } from "lodash";
import { PaymentMethodTable } from "../PaymentMethod/PaymentMethodTable";
import { fetchAllPaymentMethodAPI } from "../../service/PaymentMethodService";
import CreatePaymentMethodModal from "../PaymentMethod/CreatePaymentMethodModal";

import UpdatePaymentMethodModal from "../PaymentMethod/UpdatePaymentMethodModal";
// import {Modal, Form, Input, Switch} from 'antd';

export const PaymentMethodPage = () => {
    const { setBreadcrumbItems } = useOutletContext();

    // const [Form] = Form.useForm();

    const [paymentMethods, setPaymentMethods] = useState([]);
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState(null);
    const [refreshFlag, setRefreshFlag] = useState(false);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
    const [selectedId, setSelectedId] = useState(null);
    const [datamethod, setDatamethod] = useState(null);

    useEffect(() => {
        setBreadcrumbItems([
            { title: "Trang chủ", href: "/admin" },
            { title: "Quản lý thanh toán" },
            { title: "Phương thức thanh toán" },
        ]);
    }, []);

    useEffect(() => {
        const loadPaymentMethods = async () => {
            try {
                const res = await fetchAllPaymentMethodAPI();
                console.log(res);
                if (res && res.data) {
                    setPaymentMethods(res.data);
                }
            } catch (error) {
                console.error("Error loading payment methods:", error);
            }
        };

        loadPaymentMethods();
    }, [refreshFlag]);

    const filteredMethods = paymentMethods.filter((pm) => {
        const matchSearch = pm.name.toLowerCase().includes(search.toLowerCase());
        const matchStatus = statusFilter === undefined || pm.active === statusFilter || statusFilter === null;
        return matchSearch && matchStatus;
    });

    const handleSearch = debounce((value) => {
        setSearch(value);
    }, 300);

    return (
        <div
            style={{
                padding: 24,
                background: "#fff",
                borderRadius: "8px",
                boxShadow: "0 2px 8px rgba(0,0,0,0.1)",
            }}
        >
            <div className="flex justify-between mb-4">
                <div style={{ display: "flex", gap: "2rem" }}>
                    <Input
                        style={{ width: "30vw" }}
                        size="large"
                        addonBefore={<SearchOutlined />}
                        placeholder="Tìm kiếm phương thức..."
                        allowClear
                        onChange={(e) => handleSearch(e.target.value)}
                    />
                    <Select
                        size="large"
                        style={{ width: "10vw" }}
                        options={[
                            { value: true, label: "Đang hoạt động" },
                            { value: false, label: "Vô hiệu hóa" },
                        ]}
                        placeholder="Trạng thái"
                        allowClear
                        onChange={(value) => setStatusFilter(value)}
                    />
                </div>
                <Button onClick={() => setIsCreateModalOpen(true)} size="large" type="primary">
                    <TicketPlus size={20} strokeWidth={1.5} />
                    <span>Thêm phương thức</span>
                </Button>

                <CreatePaymentMethodModal
                    isCreateModalOpen={isCreateModalOpen}
                    setIsCreateModalOpen={setIsCreateModalOpen}
                    setRefreshFlag={setRefreshFlag}
                />

                <UpdatePaymentMethodModal
                    visible={isUpdateModalOpen}
                    setVisible={setIsUpdateModalOpen}
                    methodId={selectedId}
                    setRefreshFlag={setRefreshFlag}
                />
            </div>

            <PaymentMethodTable
                data={filteredMethods}
                setRefreshFlag={setRefreshFlag}
                onEdit={(id) => {
                    setSelectedId(id);
                    setIsUpdateModalOpen(true);
                }}
            />
        </div>
    );
};
