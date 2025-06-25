import { Table, Tag } from "antd";
import { fetchAllPaymentMethodAPI } from '../../service/PaymentMethodService';

export const PaymentMethodTable = ({ data, setRefreshFlag }) => {
    const columns = [
        { title: 'ID', dataIndex: 'id', key: 'id', width: 80 },
        { title: 'Tên phương thức', dataIndex: 'name', key: 'name' },
        { title: 'Mô tả', dataIndex: 'description', key: 'description' },
        {
            title: 'Trạng thái', dataIndex: 'active', key: 'active',
            render: (active) => (
                <Tag color={active ? 'green' : 'red'}>
                    {active ? 'Đang hoạt động' : 'Vô hiệu hóa'}
                </Tag>
            )
        }
    ];

    return (
        <Table
            rowKey="id"
            columns={columns}
            dataSource={data}
            pagination={false} // Không phân trang
        />
    );
};
