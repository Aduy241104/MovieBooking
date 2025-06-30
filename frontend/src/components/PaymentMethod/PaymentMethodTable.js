import { Table, Tag, Button, Popconfirm, message, Space, Tooltip } from "antd";
import { DeleteOutlined, EditOutlined } from '@ant-design/icons';
import { SquarePen, Lock, LockOpen, Trash2 } from "lucide-react";
import axios from "axios";

export const PaymentMethodTable = ({ data, setRefreshFlag, onEdit }) => {
    const handleDelete = async (id) => {
        try {
            const token = localStorage.getItem('token');
            await axios.delete(`http://localhost:8081/api/admin/payment-methods/${id}`, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            message.success("Xóa thành công!");
            setRefreshFlag(prev => !prev);
        } catch (err) {
            console.error("Lỗi khi xóa:", err);
            message.error("Xóa thất bại!");
        }
    };
    const handleToggleActive = async (id) => {
        try {
            const token = localStorage.getItem('token');
            await axios.put(`http://localhost:8081/api/admin/payment-methods/${id}/toggle`, {}, {
                headers: {
                    Authorization: `Bearer ${token}`
                }
            });
            message.success("Cập nhật trạng thái thành công!");
            setRefreshFlag(prev => !prev);
        } catch (err) {
            console.error("Lỗi khi cập nhật trạng thái:", err);
            message.error("Cập nhật trạng thái thất bại!");
        }
    };


    const columns = [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
        { title: 'Tên phương thức', dataIndex: 'name', key: 'name' },
        { title: 'Mô tả', dataIndex: 'description', key: 'description' },
        {
            title: 'Trạng thái',
            dataIndex: 'active',
            key: 'active',
            render: (active) => (
                <Tag color={active ? 'green' : 'red'}>
                    {active ? 'Đang hoạt động' : 'Vô hiệu hóa'}
                </Tag>
            )
        },
        {
            title: 'Hành động',
            key: 'action',
            width: 120,
            render: (_, record) => (
                <Space size="middle">
                    {/* Sửa */}
                    <Tooltip title="Sửa">
                        <button
                            className="text-blue-600 hover:text-fuchsia-500"
                            onClick={() => onEdit(record.id)}
                        >
                            <SquarePen size={16} strokeWidth={1.7} />
                        </button>
                    </Tooltip>

                    {/* Bật / Tắt */}
                    {record.active === false ? (
                        <Popconfirm
                            title="Khôi phục phương thức"
                            description="Bạn có muốn khôi phục không?"
                            onConfirm={() => handleToggleActive(record.id)}
                            okText="Xác nhận"
                            cancelText="Huỷ"
                        >
                            <button style={{ color: "green" }}>
                                <LockOpen size={16} strokeWidth={1.7} />
                            </button>
                        </Popconfirm>
                    ) : (
                        <Popconfirm
                            title="Vô hiệu phương thức"
                            description="Bạn có muốn vô hiệu hoá không?"
                            onConfirm={() => handleToggleActive(record.id)}
                            okText="Xác nhận"
                            cancelText="Huỷ"
                        >
                            <button style={{ color: "red" }}>
                                <Lock size={16} strokeWidth={1.7} />
                            </button>
                        </Popconfirm>
                    )}
                </Space>
            )
        }
    ];

    return (
        <Table
            rowKey="id"
            columns={columns}
            dataSource={data}
            pagination={false}
        />
    );
};
