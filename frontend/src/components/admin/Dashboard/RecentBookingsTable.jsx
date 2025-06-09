import React from 'react';
import { Table, Tag, Button } from 'antd';
import { Eye } from 'lucide-react';
import dayjs from 'dayjs';

export const RecentBookingsTable = ({ data }) => {
    const columns = [
        {
            title: 'Mã đặt vé',
            dataIndex: 'bookingCode',
            key: 'bookingCode',
            render: (code) => <span className="font-mono text-blue-600">{code}</span>
        },
        {
            title: 'Khách hàng',
            dataIndex: 'customerName',
            key: 'customerName'
        },
        {
            title: 'Phim',
            dataIndex: 'movieTitle',
            key: 'movieTitle'
        },
        {
            title: 'Suất chiếu',
            dataIndex: 'showtime',
            key: 'showtime',
            render: (time) => dayjs(time).format('DD/MM/YYYY HH:mm')
        },
        {
            title: 'Số ghế',
            dataIndex: 'seatCount',
            key: 'seatCount'
        },
        {
            title: 'Tổng tiền',
            dataIndex: 'totalAmount',
            key: 'totalAmount',
            render: (amount) => new Intl.NumberFormat('vi-VN', {
                style: 'currency',
                currency: 'VND'
            }).format(amount)
        },
        {
            title: 'Trạng thái',
            dataIndex: 'status',
            key: 'status',
            render: (status) => {
                const colors = {
                    'paid': 'green',
                    'pending': 'orange',
                    'cancelled': 'red'
                };
                const labels = {
                    'paid': 'Đã thanh toán',
                    'pending': 'Chờ thanh toán',
                    'cancelled': 'Đã hủy'
                };
                return <Tag color={colors[status]}>{labels[status]}</Tag>;
            }
        },
        {
            title: 'Thao tác',
            key: 'action',
            render: (_, record) => (
                <Button 
                    type="link" 
                    icon={<Eye size={16} />}
                    onClick={() => console.log('View booking:', record.id)}
                >
                    Xem
                </Button>
            )
        }
    ];

    // Mock data if empty
    const mockData = data.length > 0 ? data : [
        {
            id: 1,
            bookingCode: 'BK001234',
            customerName: 'Nguyễn Văn A',
            movieTitle: 'Avengers: Endgame',
            showtime: '2024-06-07T19:30:00',
            seatCount: 2,
            totalAmount: 200000,
            status: 'paid'
        },
        {
            id: 2,
            bookingCode: 'BK001235',
            customerName: 'Trần Thị B',
            movieTitle: 'Spider-Man: No Way Home',
            showtime: '2024-06-07T21:00:00',
            seatCount: 4,
            totalAmount: 400000,
            status: 'pending'
        },
        {
            id: 3,
            bookingCode: 'BK001236',
            customerName: 'Lê Văn C',
            movieTitle: 'The Batman',
            showtime: '2024-06-07T16:30:00',
            seatCount: 1,
            totalAmount: 100000,
            status: 'paid'
        }
    ];

    return (
        <Table
            columns={columns}
            dataSource={mockData}
            pagination={{ pageSize: 5 }}
            size="small"
            rowKey="id"
            scroll={{ x: 800 }}
        />
    );
};