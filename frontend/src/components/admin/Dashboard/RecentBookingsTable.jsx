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
            key: 'customerName',
            render: (name, record) => (
                <div>
                    <div className="font-semibold">{name}</div>
                    <div className="text-gray-500 text-sm">{record.customerEmail}</div>
                </div>
            )
        },
        {
            title: 'Phim',
            dataIndex: 'movieTitle',
            key: 'movieTitle'
        },
        {
            title: 'Phòng chiếu',
            dataIndex: 'cinemaRoom',
            key: 'cinemaRoom'
        },
        {
            title: 'Ngày đặt',
            dataIndex: 'bookingDate',
            key: 'bookingDate',
            render: (date) => dayjs(date).format('DD/MM/YYYY HH:mm')
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
            title: 'Thanh toán',
            dataIndex: 'paymentMethod',
            key: 'paymentMethod',
            render: (method) => <Tag color="blue">{method}</Tag>
        },
        {
            title: 'Trạng thái',
            dataIndex: 'status',
            key: 'status',
            render: (status) => {
                const colors = {
                    'PAID': 'green',
                    'PENDING': 'orange',
                    'CANCELLED': 'red'
                };
                const labels = {
                    'PAID': 'Đã thanh toán',
                    'PENDING': 'Chờ thanh toán',
                    'CANCELLED': 'Đã hủy'
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
                    onClick={() => console.log('View booking:', record.bookingId)}
                >
                    Xem
                </Button>
            )
        }
    ];

    // Mock data if empty
    const mockData = data.length > 0 ? data : [
        {
            bookingId: 1,
            bookingCode: 'BK001234',
            customerName: 'Nguyễn Văn A',
            customerEmail: 'nguyenvana@email.com',
            movieTitle: 'Avengers: Endgame',
            cinemaRoom: 'Phòng 1',
            bookingDate: '2024-06-07T19:30:00',
            seatCount: 2,
            totalAmount: 200000,
            paymentMethod: 'VNPAY',
            status: 'PAID'
        },
        {
            bookingId: 2,
            bookingCode: 'BK001235',
            customerName: 'Trần Thị B',
            customerEmail: 'tranthib@email.com',
            movieTitle: 'Spider-Man: No Way Home',
            cinemaRoom: 'Phòng 2',
            bookingDate: '2024-06-07T21:00:00',
            seatCount: 4,
            totalAmount: 400000,
            paymentMethod: 'Momo',
            status: 'PENDING'
        },
        {
            bookingId: 3,
            bookingCode: 'BK001236',
            customerName: 'Lê Văn C',
            customerEmail: 'levanc@email.com',
            movieTitle: 'The Batman',
            cinemaRoom: 'Phòng 3',
            bookingDate: '2024-06-07T16:30:00',
            seatCount: 1,
            totalAmount: 100000,
            paymentMethod: 'ZaloPay',
            status: 'PAID'
        }
    ];

    return (
        <Table
            columns={columns}
            dataSource={mockData}
            pagination={{ pageSize: 5 }}
            size="small"
            rowKey="bookingId"
            scroll={{ x: 1000 }}
        />
    );
};