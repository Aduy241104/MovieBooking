import { message, Pagination, Popconfirm, Space, Table, Tag } from "antd";
import dayjs from "dayjs";
import { Lock, LockOpen, SquarePen } from "lucide-react";
import { useState } from "react";
import { useLocation, useNavigate, useOutletContext } from 'react-router-dom';
import { updateStatusAccountAPI } from "../../../service/AccountService";



export const UserTable = () => {
    const { dataAccount, page, setPage, size, total, setRefreshFlag } = useOutletContext();
    const navigate = useNavigate();
    const location = useLocation();

    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
    const [dataUser, setDataUser] = useState("");

    const handleTableChange = (pagination) => {
        if (pagination)
            setPage(pagination)
    }

    const handleUpdateIsActiveAccount = async (record) => {
        const res = await updateStatusAccountAPI(record.accountId, !record.active)
        if (res.data) {
            message.success(
                <span>
                    {record.active ? 'Mở khoá tài khoản ' : 'Khoá tài khoản '}
                    <span className='font-medium'>{record.email}</span>
                    {' thành công'}
                </span>
            );
            setRefreshFlag(prev => !prev);
            return;
        }
        message.error(`Error: ${res.error}`)
    };

    const handleViewUserDetail = async (id) => {
        navigate(`${location.pathname}/${id}`)

    }

    const columns = [
        {
            title: 'STT',
            render: (_, render, index) => (
                <>
                    {(page - 1) * size + index + 1}
                </>
            ),
        },
        {
            title: 'Tài khoản',
            dataIndex: 'fullName',
            render: (text, render) => (
                <>
                    <div className='flex flex-col'>
                        <p className='font-medium text-gray-500 hover:text-blue-600 cursor-pointer hover:underline transition duration-200'
                            onClick={() => handleViewUserDetail(render.accountId)}
                        >
                            {text}
                        </p>
                        <p className='text-gray-600'>{render.email}</p>
                    </div>
                </>
            ),
        },
        {
            title: 'Ngày sinh',
            dataIndex: 'birthday',
            render: (text) => (
                <>
                    {text ? dayjs(text).format('DD/MM/YYYY') : "Không có dữ liệu"}
                </>
            ),
        },
        {
            title: 'Giới tính',
            dataIndex: 'gender',
            render: (text) => (
                <>
                    <Tag color={text === "Female" ? "#1677ff" : text === "Male" ? "#f759ab" : "#9254de"}>
                        {text === "Female" ? "Nam" : text === "Male" ? "Nữ" : "Khác"}
                    </Tag>
                </>
            ),
        },
        {
            title: 'Điện thoại',
            dataIndex: 'phone',
        },
        {
            title: 'Trạng thái',
            render: (_, record) => (
                <>
                    <Tag color={record.active ? "volcano" : "green"}>
                        {record.active ? "Đã khoá" : "Hoạt động"}
                    </Tag>
                </>
            ),
        },
        {
            title: 'Action',
            render: (_, record) => (
                <>
                    <Space size="large">
                        <a onClick={() => {
                            setIsUpdateModalOpen(true);
                            setDataUser(record);
                        }}>
                            <SquarePen size={16} strokeWidth={1.7} />
                        </a>
                        {record.active ? (
                            <>
                                <Popconfirm
                                    placement="left"
                                    title="Mở khoá tài khoản"
                                    description="Xác nhận mở khoá?"
                                    onConfirm={() => handleUpdateIsActiveAccount(record)}
                                    okText="Xác nhận"
                                    cancelText="Huỷ"
                                >
                                    <a style={{ color: "green" }}>
                                        <LockOpen size={16} strokeWidth={1.7} />
                                    </a>
                                </Popconfirm>
                            </>
                        ) : (
                            <>
                                <Popconfirm
                                    placement="left"
                                    title="Khoá tài khoản"
                                    description="Xác nhận khoá?"
                                    onConfirm={() => handleUpdateIsActiveAccount(record)}
                                    okText="Xác nhận"
                                    cancelText="Huỷ"
                                >
                                    <a style={{ color: "red" }}>
                                        <Lock size={16} strokeWidth={1.7} />
                                    </a>
                                </Popconfirm>
                            </>
                        )}

                    </Space>
                </>

            ),
        },
    ];

    return (
        <>
            <Table
                columns={columns}
                dataSource={dataAccount}
                rowKey={"accountId"}
                pagination={false}
            />

            <div className='flex justify-center mt-8'>
                <Pagination
                    current={page}
                    pageSize={size}
                    total={total}
                    showTotal={(total, range) => `${range[0]}-${range[1]} trong ${total} mục`}
                    onChange={handleTableChange}
                />
            </div>
        </>
    );
}