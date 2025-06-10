import React from 'react';
import { Table, Avatar } from 'antd';
import { Star, Eye, DollarSign } from 'lucide-react';

export const TopMoviesTable = ({ data = [] }) => {
    const columns = [
        {
            title: '#',
            dataIndex: 'rank',
            key: 'rank',
            width: 50,
            render: (text, record, index) => index + 1
        },
        {
            title: 'Phim',
            dataIndex: 'title',
            key: 'title',
            render: (text, record) => (
                <div className="flex items-center gap-3">
                    <Avatar src={record.poster} size="large" />
                    <div>
                        <div className="font-semibold">{text}</div>
                        <div className="text-gray-500 text-sm">{record.genre}</div>
                    </div>
                </div>
            )
        },
        {
            title: 'Đánh giá',
            dataIndex: 'rating',
            key: 'rating',
            render: (rating) => (
                <div className="flex items-center gap-1">
                    <Star size={16} fill="#faad14" color="#faad14" />
                    <span>{rating}/5</span>
                </div>
            )
        },
        {
            title: 'Vé đã bán',
            dataIndex: 'totalBookings',
            key: 'totalBookings',
            render: (bookings) => (
                <div className="flex items-center gap-1">
                    <Eye size={16} color="#1890ff" />
                    <span>{bookings?.toLocaleString() || 0}</span>
                </div>
            )
        },
        {
            title: 'Doanh thu',
            dataIndex: 'revenue',
            key: 'revenue',
            render: (revenue) => (
                <div className="flex items-center gap-1">
                    <DollarSign size={16} color="#52c41a" />
                    <span>{new Intl.NumberFormat('vi-VN').format(revenue || 0)} VNĐ</span>
                </div>
            )
        }
    ];

    // Mock data if empty
    const tableData = Array.isArray(data) && data.length > 0 ? data : [
        { id: 1, title: 'Avengers: Endgame', genre: 'Hành động', rating: 4.8, totalBookings: 542, revenue: 15420000, poster: 'https://via.placeholder.com/40' },
        { id: 2, title: 'Spider-Man: No Way Home', genre: 'Hành động', rating: 4.6, totalBookings: 423, revenue: 12340000, poster: 'https://via.placeholder.com/40' },
        { id: 3, title: 'The Batman', genre: 'Hành động', rating: 4.4, totalBookings: 387, revenue: 10890000, poster: 'https://via.placeholder.com/40' },
        { id: 4, title: 'Top Gun: Maverick', genre: 'Hành động', rating: 4.5, totalBookings: 356, revenue: 9876000, poster: 'https://via.placeholder.com/40' },
    ];

    return (
        <Table
            columns={columns}
            dataSource={tableData}
            pagination={false}
            size="small"
            rowKey="id"
            scroll={{ x: 600 }}
        />
    );
};