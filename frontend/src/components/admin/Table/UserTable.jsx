import { message, Pagination, Popconfirm, Space, Table, Tag } from "antd";
import dayjs from "dayjs";
import { Lock, LockOpen, SquarePen, Trash2 } from "lucide-react";
import { useState } from "react";
import { useLocation, useNavigate, useOutletContext } from 'react-router-dom';
import { deleteAccountAPI, updateAccountStatusAPI } from "../../../service/AccountService";
import { UpdateUserModal } from "../Modal/users/UpdateUserModal";



export const UserTable = (props) => {
    const { dataUsers, page, setPage, size, total, setRefreshFlag } = useOutletContext();

    const { userText, userRole } = props;

    const navigate = useNavigate();
    const location = useLocation();

    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
    const [dataUser, setDataUser] = useState("");

    const handleTableChange = (pagination) => {
        if (pagination)
            setPage(pagination)
    }

    const handleUpdateStatusAccount = async (record) => {
        const res = await updateAccountStatusAPI(record.accountId, record.status === 0 ? 1 : 0)
        if (res.result) {
            message.success(
                <span>
                    {record.status === 0 ? 'Mở khoá tài khoản ' : 'Khoá tài khoản '}
                    <span className='font-medium'>{record.email}</span>
                    {' thành công'}
                </span>
            );
            setRefreshFlag(prev => !prev);
            return;
        }
        message.error(`Error: ${res.message}`)
    };

    const handleViewUserDetail = async (accountId) => {
        navigate(`${location.pathname}/${accountId}`);

    }

    const handleDeleteAccount = async (record) => {
        const res = await deleteAccountAPI(record.accountId);
        if (res.result) {
            message.success(`Tài khoản ${record.email} đã được xoá thành công`);
            setRefreshFlag(prev => !prev);
            return;
        }
        message.error(`Error: ${res.message}`)
    };


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
            dataIndex: 'dateOfBirth',
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
                    <Tag color={text === "Nam" ? "#1677ff" : text === "Nữ" ? "#f759ab" : "#9254de"}>
                        {text}
                    </Tag>
                </>
            ),
        },
        {
            title: 'Điện thoại',
            dataIndex: 'phoneNumber',
        },
        {
            title: 'Trạng thái',
            render: (_, record) => (
                <>
                    <Tag color={record.status === 0 ? "volcano" : "green"}>
                        {record.status === 0 ? "Đã khoá" : "Hoạt động"}
                    </Tag>
                </>
            ),
        },
        {
            title: 'Hành động',
            render: (_, record) => (
                <>
                    <Space size="large">
                        <button className="text-blue-600 hover:text-fuchsia-500" onClick={() => {
                            setIsUpdateModalOpen(true);
                            setDataUser(record);
                        }}>
                            <SquarePen size={16} strokeWidth={1.7} />
                        </button>
                        {record.status === 0 ? (
                            <>
                                <Popconfirm
                                    placement="left"
                                    title="Mở khoá tài khoản"
                                    description="Xác nhận mở khoá?"
                                    onConfirm={() => handleUpdateStatusAccount(record)}
                                    okText="Xác nhận"
                                    cancelText="Huỷ"
                                >
                                    <button style={{ color: "green" }}>
                                        <LockOpen size={16} strokeWidth={1.7} />
                                    </button>
                                </Popconfirm>
                            </>
                        ) : (
                            <>
                                <Popconfirm
                                    placement="left"
                                    title="Khoá tài khoản"
                                    description="Xác nhận khoá?"
                                    onConfirm={() => handleUpdateStatusAccount(record)}
                                    okText="Xác nhận"
                                    cancelText="Huỷ"
                                >
                                    <button style={{ color: "red" }}>
                                        <Lock size={16} strokeWidth={1.7} />
                                    </button>
                                </Popconfirm>
                            </>
                        )}

                        <Popconfirm
                            placement="left"
                            title="Xoá tài khoản"
                            description="Xác nhận xoá?"
                            onConfirm={() => handleDeleteAccount(record)}
                            okText="Xoá"
                            cancelText="Huỷ"
                        >
                            <button className="text-amber-600 hover:text-amber-700">
                                <Trash2 size={16} strokeWidth={1.7} />
                            </button>
                        </Popconfirm>

                    </Space>
                </>

            ),
        },
    ];

    return (
        <>
            <Table
                columns={columns}
                dataSource={dataUsers}
                rowKey={"accountId"}
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

            <UpdateUserModal
                isUpdateModalOpen={isUpdateModalOpen}
                setIsUpdateModalOpen={setIsUpdateModalOpen}
                dataUser={dataUser}
                setDataUser={setDataUser}
                userText={userText}
                userRole={userRole}
            />
        </>
    );
}