import { useEffect, useState } from "react";
import { LoadingOutlined } from "@ant-design/icons";
import { message, Pagination, Spin, Table, Tag } from "antd";
import { fetchActivityLogsAPI } from "../../../service/ActivityLogService";
import dayjs from "dayjs";

export const ActivityLogTable = ({ filter }) => {
    const [logs, setLogs] = useState([]);
    const [loading, setLoading] = useState(false);
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(10);
    const [total, setTotal] = useState(0);

    useEffect(() => {
        setLoading(true);
        fetchActivityLogsAPI(page, size, filter)
            .then((res) => {
                setLogs(res.result.data);
                setTotal(res.result.meta.total);
                setLoading(false);
            })
            .catch((error) => {
                if (error.message === "Network Error") {
                    message.error("Không thể kết nối tới máy chủ. Vui lòng kiểm tra lại kết nối hoặc thử lại sau!");
                } else {
                    message.error(`Đã xảy ra lỗi: ${error.message}. Vui lòng thử lại sau!`);
                }
            });
    }, [page, size, filter]);

    const handleTableChange = (pagination) => {
        if (pagination) setPage(pagination);
    };

    const columns = [
        {
            title: "STT",
            width: 60,
            render: (text, render, index) => <>{(page - 1) * size + index + 1}</>,
        },
        {
            title: "Người thao tác",
            dataIndex: "updatedBy",
        },
        {
            title: "Đối tượng",
            dataIndex: "entityType",
            width: 140,
            render: (text) => (
                <>
                    <Tag color={text === "NHÂN VIÊN" ? "#607D8B" : text === "THÀNH VIÊN" ? "#2196F3" : "pink"}>
                        {text}
                    </Tag>
                </>
            ),
        },
        {
            title: "Ảnh hưởng",
            dataIndex: "userUpdated",
        },
        {
            title: "Mô tả",
            dataIndex: "description",
        },
        {
            title: "Hành động",
            dataIndex: "action",
            width: 120,
            render: (text) => (
                <>
                    <Tag color={text === "XOÁ" ? "volcano" : text === "CẬP NHẬT" ? "blue" : "green"}>
                        {text === "XOÁ" ? "XOÁ" : text === "CẬP NHẬT" ? "CẬP NHẬT" : "TẠO MỚI"}
                    </Tag>
                </>
            ),
        },
        {
            title: "Thời gian",
            dataIndex: "createdAt",
            width: 160,
            render: (text) => <>{text ? dayjs(text).format("DD/MM/YYYY HH:mm") : "N/A"}</>,
        },
    ];

    return (
        <>
            {loading ? (
                <div className="flex flex-col justify-center items-center gap-3 h-screen">
                    <Spin indicator={<LoadingOutlined spin />} size="large" />
                    <span className="text-xl font-semibold">Đang tải dữ liệu...</span>
                </div>
            ) : (
                <>
                    <Table
                        columns={columns}
                        dataSource={logs}
                        // loading={loading}
                        rowKey="id"
                        pagination={false}
                    />

                    <div className="flex justify-center mt-4">
                        <Pagination
                            current={page}
                            pageSize={size}
                            total={total}
                            showTotal={(total, range) => `${range[0]}-${range[1]} trong ${total} mục`}
                            onChange={handleTableChange}
                        />
                    </div>
                </>
            )}
        </>
    );
};
