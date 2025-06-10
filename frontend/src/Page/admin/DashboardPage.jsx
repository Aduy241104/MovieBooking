import React, { useState, useEffect } from 'react';
import { Row, Col, Card, Statistic, Spin } from 'antd';
import { TrendingUp, Users, Calendar, DollarSign, Eye, Star, UserCheck, Film } from 'lucide-react';
import { RevenueChart } from '../../components/admin/Dashboard/RevenueChart';
import { MovieChart } from '../../components/admin/Dashboard/MovieChart';
import { UserChart } from '../../components/admin/Dashboard/UserChart';
import { TopMoviesTable } from '../../components/admin/Dashboard/TopMoviesTable';
import { RecentBookingsTable } from '../../components/admin/Dashboard/RecentBookingsTable';

export const DashboardPage = () => {
    const [loading, setLoading] = useState(true);
    const [revenueTimeRange, setRevenueTimeRange] = useState('month');
    const [dashboardData, setDashboardData] = useState({
        stats: {
            totalRevenue: 0,
            totalBookings: 0,
            totalUsers: 0,
            activeMovies: 0,
            totalReviews: 0,
            avgRating: 0,
            activePromotions: 0,
            totalCinemaRooms: 0
        },
        charts: {
            revenue: {
                day: [],
                week: [],
                month: []
            },
            moviesByType: [],
            userRegistrations: [],
            bookingsByPaymentMethod: []
        },
        tables: {
            topMovies: [],
            recentBookings: []
        }
    });

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        setLoading(true);
        try {
            // Mock data phù hợp với database của bạn
            const mockData = {
                stats: {
                    totalRevenue: 125000000,
                    totalBookings: 1543,
                    totalUsers: 2847,
                    activeMovies: 12,
                    totalReviews: 3456,
                    avgRating: 4.3,
                    activePromotions: 5,
                    totalCinemaRooms: 8
                },
                charts: {
                    revenue: {
                        day: [
                            { name: '01/06', revenue: 2500000, bookings: 45 },
                            { name: '02/06', revenue: 3200000, bookings: 52 },
                            { name: '03/06', revenue: 2800000, bookings: 48 },
                            { name: '04/06', revenue: 4100000, bookings: 68 },
                            { name: '05/06', revenue: 3600000, bookings: 55 },
                            { name: '06/06', revenue: 4800000, bookings: 72 },
                            { name: '07/06', revenue: 5200000, bookings: 83 },
                        ],
                        week: [
                            { name: 'Tuần 1', revenue: 18500000, bookings: 320 },
                            { name: 'Tuần 2', revenue: 22300000, bookings: 356 },
                            { name: 'Tuần 3', revenue: 19800000, bookings: 334 },
                            { name: 'Tuần 4', revenue: 25600000, bookings: 389 },
                            { name: 'Tuần 5', revenue: 28200000, bookings: 423 },
                            { name: 'Tuần 6', revenue: 31000000, bookings: 467 },
                        ],
                        month: [
                            { name: 'T1', revenue: 85000000, bookings: 1200 },
                            { name: 'T2', revenue: 123000000, bookings: 1560 },
                            { name: 'T3', revenue: 98000000, bookings: 1340 },
                            { name: 'T4', revenue: 156000000, bookings: 1890 },
                            { name: 'T5', revenue: 182000000, bookings: 2230 },
                            { name: 'T6', revenue: 210000000, bookings: 2670 },
                        ]
                    },
                    moviesByType: [
                        { name: 'Hành động', count: 8, revenue: 45000000 },
                        { name: 'Hài', count: 6, revenue: 32000000 },
                        { name: 'Kinh dị', count: 4, revenue: 28000000 },
                        { name: 'Tình cảm', count: 3, revenue: 20000000 },
                        { name: 'Khoa học viễn tưởng', count: 2, revenue: 15000000 },
                    ],
                    userRegistrations: [
                        { name: 'T1', newUsers: 145, totalUsers: 1200 },
                        { name: 'T2', newUsers: 267, totalUsers: 1467 },
                        { name: 'T3', newUsers: 354, totalUsers: 1821 },
                        { name: 'T4', newUsers: 278, totalUsers: 2099 },
                        { name: 'T5', newUsers: 389, totalUsers: 2488 },
                        { name: 'T6', newUsers: 359, totalUsers: 2847 },
                    ],
                    bookingsByPaymentMethod: [
                        { name: 'VNPAY', count: 45, revenue: 55000000 },
                        { name: 'Momo', count: 35, revenue: 42000000 },
                        { name: 'ZaloPay', count: 20, revenue: 28000000 },
                    ]
                },
                tables: {
                    topMovies: [
                        { id: 1, title: 'Avengers: Endgame', genre: 'Hành động', rating: 4.8, totalBookings: 542, revenue: 15420000, poster: 'https://via.placeholder.com/40' },
                        { id: 2, title: 'Spider-Man: No Way Home', genre: 'Hành động', rating: 4.6, totalBookings: 423, revenue: 12340000, poster: 'https://via.placeholder.com/40' },
                        { id: 3, title: 'The Batman', genre: 'Hành động', rating: 4.4, totalBookings: 387, revenue: 10890000, poster: 'https://via.placeholder.com/40' },
                        { id: 4, title: 'Top Gun: Maverick', genre: 'Hành động', rating: 4.5, totalBookings: 356, revenue: 9876000, poster: 'https://via.placeholder.com/40' },
                    ],
                    recentBookings: [
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
                        }
                    ]
                }
            };

            setTimeout(() => {
                setDashboardData(mockData);
                setLoading(false);
            }, 500);
        } catch (error) {
            console.error('Error loading dashboard data:', error);
            setLoading(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-96">
                <Spin size="large" />
            </div>
        );
    }

    return (
        <div className="space-y-6">
            {/* Statistics Cards */}
            <Row gutter={[16, 16]}>
                <Col xs={24} sm={12} lg={6}>
                    <Card>
                        <Statistic
                            title="Tổng Doanh Thu"
                            value={dashboardData.stats.totalRevenue}
                            precision={0}
                            valueStyle={{ color: '#3f8600' }}
                            prefix={<DollarSign size={20} />}
                            suffix="VNĐ"
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <Card>
                        <Statistic
                            title="Tổng Đặt Vé"
                            value={dashboardData.stats.totalBookings}
                            valueStyle={{ color: '#cf1322' }}
                            prefix={<Calendar size={20} />}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <Card>
                        <Statistic
                            title="Tổng Thành Viên"
                            value={dashboardData.stats.totalUsers}
                            valueStyle={{ color: '#1890ff' }}
                            prefix={<Users size={20} />}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <Card>
                        <Statistic
                            title="Phim Đang Chiếu"
                            value={dashboardData.stats.activeMovies}
                            valueStyle={{ color: '#722ed1' }}
                            prefix={<Film size={20} />}
                        />
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]}>
                <Col xs={24} sm={12} lg={6}>
                    <Card>
                        <Statistic
                            title="Tổng Đánh Giá"
                            value={dashboardData.stats.totalReviews}
                            valueStyle={{ color: '#fa8c16' }}
                            prefix={<Eye size={20} />}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <Card>
                        <Statistic
                            title="Đánh Giá TB"
                            value={dashboardData.stats.avgRating}
                            precision={1}
                            valueStyle={{ color: '#faad14' }}
                            prefix={<Star size={20} />}
                            suffix="/5"
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <Card>
                        <Statistic
                            title="Khuyến Mãi Hiện Tại"
                            value={dashboardData.stats.activePromotions}
                            valueStyle={{ color: '#52c41a' }}
                            prefix={<TrendingUp size={20} />}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <Card>
                        <Statistic
                            title="Tổng Phòng Chiếu"
                            value={dashboardData.stats.totalCinemaRooms}
                            valueStyle={{ color: '#13c2c2' }}
                            prefix={<UserCheck size={20} />}
                        />
                    </Card>
                </Col>
            </Row>

            {/* Charts */}
            <Row gutter={[16, 16]}>
                <Col xs={24} lg={16}>
                    <Card title="Biểu Đồ Doanh Thu & Đặt Vé">
                        <RevenueChart
                            data={dashboardData.charts.revenue[revenueTimeRange]}
                            timeRange={revenueTimeRange}
                            onTimeRangeChange={setRevenueTimeRange}
                        />
                    </Card>
                </Col>
                <Col xs={24} lg={8}>
                    <Card title="Phân Loại Phim Theo Thể Loại">
                        <MovieChart data={dashboardData.charts.moviesByType} />
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]}>
                <Col xs={24} lg={12}>
                    <Card title="Thống Kê Đăng Ký Thành Viên">
                        <UserChart data={dashboardData.charts.userRegistrations} />
                    </Card>
                </Col>
                <Col xs={24} lg={12}>
                    <Card title="Top Phim Có Doanh Thu Cao">
                        <TopMoviesTable data={dashboardData.tables?.topMovies || []} />
                    </Card>
                </Col>
            </Row>

            <Row>
                <Col span={24}>
                    <Card title="Đặt Vé Gần Đây">
                        <RecentBookingsTable data={dashboardData.tables?.recentBookings || []} />
                    </Card>
                </Col>
            </Row>
        </div>
    );
};