import React, { useEffect, useState } from "react";
import { SearchOutlined } from "@ant-design/icons";
import { Table, Input, Select, message } from "antd";
import { useOutletContext } from "react-router-dom";
import { debounce } from "lodash";
import axios from "axios";
import PaymentTransactionService, { getAllTransactions } from '../../service/PaymentTransactionService';

const PaymentTransactionPage = () => {
    const { setBreadcrumbItems } = useOutletContext();

    const [transactions, setTransactions] = useState([]);
    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState(null);
    const [loading, setLoading] = useState(true);
    const [currentPage, setCurrentPage] = useState(0); // hoặc 1 nếu bạn muốn bắt đầu từ trang 1
    const [totalPages, setTotalPages] = useState(0);

    useEffect(() => {
        setBreadcrumbItems([
            { title: "Trang chủ", href: "/admin" },
            { title: "Giao dịch thanh toán" }
        ]);
    }, []);

    useEffect(() => {
        const fetchTransactions = async () => {
            try {
                const tokenStr = localStorage.getItem("token");
                const token = tokenStr?.startsWith('"') ? JSON.parse(tokenStr) : tokenStr;

                const res = await axios.get(`http://localhost:8081/api/admin/payment-transactions?page=${currentPage}&size=10`, {
                    headers: {
                        Authorization: `Bearer ${token}`
                    }
                });

                setTransactions(res.data.content || []);
                setTotalPages(res.data.totalPages); // nếu cần
            } catch (err) {
                console.error("Lỗi khi tải giao dịch:", err);
                message.error("Không thể tải dữ liệu giao dịch.");
            } finally {
                setLoading(false);
            }
        };

        fetchTransactions();
    }, [currentPage]);


    const handleSearch = debounce((value) => {
        setSearch(value);
    }, 300);

    const filteredData = transactions.filter((t) => {
        const searchLower = search.toLowerCase();
        const matchSearch =
            String(t.bookingId).includes(searchLower) ||
            (t.accountName && t.accountName.toLowerCase().includes(searchLower)) ||
            (t.paymentMethod && t.paymentMethod.toLowerCase().includes(searchLower));

        const matchStatus = statusFilter === null || t.bookingStatus === statusFilter;

        return matchSearch && matchStatus;
    });

    const columns = [
        {
            title: "Mã giao dịch",
            dataIndex: "bookingId",
            key: "bookingId"
        },
        {
            title: "Khách hàng",
            dataIndex: "accountName",
            key: "accountName"
        },
        {
            title: "Phương thức",
            dataIndex: "paymentMethod",
            key: "paymentMethod"
        },
        {
            title: "Tổng tiền",
            dataIndex: "totalAmount",
            key: "totalAmount",
            render: (amount) => `${amount?.toLocaleString()} VND`
        },
        {
            title: "Trạng thái",
            dataIndex: "bookingStatus",
            key: "bookingStatus"
        },
        {
            title: "Thời gian",
            dataIndex: "bookingTime",
            key: "bookingTime",
            render: (time) => new Date(time).toLocaleString("vi-VN")
        }
    ];

    return (
        <div
            style={{
                padding: 24,
                background: "#fff",
                borderRadius: 8,
                boxShadow: "0 2px 8px rgba(0,0,0,0.1)"
            }}
        >
            <div style={{ display: "flex", justifyContent: "space-between", marginBottom: 16 }}>
                <div style={{ display: "flex", gap: 20 }}>
                    <Input
                        style={{ width: 300 }}
                        size="large"
                        placeholder="Tìm theo mã, tên, phương thức..."
                        allowClear
                        prefix={<SearchOutlined />}
                        onChange={(e) => handleSearch(e.target.value)}
                    />

                    <Select
                        size="large"
                        style={{ width: 180 }}
                        allowClear
                        placeholder="Lọc theo trạng thái"
                        onChange={(value) => setStatusFilter(value)}
                        options={[
                            { value: "PAID", label: "Đã thanh toán" },
                            { value: "PENDING", label: "Chờ thanh toán" },
                            { value: "CANCELLED", label: "Đã hủy" }
                        ]}
                    />
                </div>
            </div>

            <Table
                dataSource={filteredData}
                columns={columns}
                loading={loading}
                rowKey="bookingId"
                pagination={{
                    current: currentPage , // Vì backend page = 0, còn Table bắt đầu từ 1
                    pageSize: 10,
                    total: totalPages * 10,   // tổng số dòng
                    onChange: (page) => setCurrentPage(page - 1),
                }}
            />
        </div>
    );
};

export default PaymentTransactionPage;
