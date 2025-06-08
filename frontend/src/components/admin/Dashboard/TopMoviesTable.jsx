import React from 'react';
import { Table, Avatar } from 'antd';
import { Star } from 'lucide-react';

export const TopMoviesTable = ({ data = [] }) => { // Default parameter
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
            title: 'Lượt xem',
            dataIndex: 'views',
            key: 'views',
            render: (views) => views?.toLocaleString() || 0
        }
    ];

    // Mock data if empty - ensure it's always an array
    const tableData = Array.isArray(data) && data.length > 0 ? data : [
        { id: 1, title: 'Avengers: Endgame', genre: 'Hành động', rating: 4.8, views: 15420, poster: 'https://via.placeholder.com/40' },
        { id: 2, title: 'Spider-Man: No Way Home', genre: 'Hành động', rating: 4.6, views: 12340, poster: 'https://via.placeholder.com/40' },
        { id: 3, title: 'The Batman', genre: 'Hành động', rating: 4.4, views: 10890, poster: 'https://via.placeholder.com/40' },
        { id: 4, title: 'Top Gun: Maverick', genre: 'Hành động', rating: 4.5, views: 9876, poster: 'https://via.placeholder.com/40' },
    ];

    return (
        <Table
            columns={columns}
            dataSource={tableData}
            pagination={false}
            size="small"
            rowKey="id"
        />
    );
};