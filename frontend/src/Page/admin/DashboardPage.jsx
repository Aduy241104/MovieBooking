import { useState, useEffect } from "react";
import { Row, Col, Card, Statistic, Spin, message } from "antd";
import { TrendingUp, Users, Calendar, DollarSign, Eye, Star, UserCheck, Film } from "lucide-react";
import { RevenueChart } from "../../components/admin/Dashboard/RevenueChart";
import { MovieChart } from "../../components/admin/Dashboard/MovieChart";
import { UserChart } from "../../components/admin/Dashboard/UserChart";
import { TopMoviesTable } from "../../components/admin/Dashboard/TopMoviesTable";
import { RecentBookingsTable } from "../../components/admin/Dashboard/RecentBookingsTable";
import axios from "../../config/axios.js";

export const DashboardPage = () => {
    const [loading, setLoading] = useState(true);
    const [revenueTimeRange, setRevenueTimeRange] = useState("month");
    const [dashboardData, setDashboardData] = useState({
        stats: {
            totalRevenue: 0,
            totalBookings: 0,
            totalUsers: 0,
            activeMovies: 0,
            totalReviews: 0,
            avgRating: 0,
            activePromotions: 0,
            totalCinemaRooms: 0,
        },
        charts: {
            revenue: {
                day: [],
                week: [],
                month: [],
            },
            moviesByType: [],
            userRegistrations: [],
            bookingsByPaymentMethod: [],
        },
        tables: {
            topMovies: [],
            recentBookings: [],
        },
    });

    useEffect(() => {
        loadDashboardData();
    }, []);

    const loadDashboardData = async () => {
        setLoading(true);
        try {
            const dashboardSummary = await axios.get("/admin/dashboard/summary");
            // Mock data phù hợp với database của bạn
            const mockData = {
                stats: {
                    totalRevenue: dashboardSummary.data.totalRevenue,
                    totalBookings: dashboardSummary.data.totalBookings,
                    totalUsers: dashboardSummary.data.totalUsers,
                    activeMovies: dashboardSummary.data.activeMovies,
                    totalReviews: dashboardSummary.data.totalReviews,
                    avgRating: dashboardSummary.data.avgRating,
                    activePromotions: dashboardSummary.data.activePromotions,
                    totalCinemaRooms: dashboardSummary.data.totalCinemaRooms,
                },
                charts: {
                    revenue: {
                        day: dashboardSummary.data.charts.revenue.day,
                        week: dashboardSummary.data.charts.revenue.week,
                        month: dashboardSummary.data.charts.revenue.month,
                    },
                    moviesByType: dashboardSummary.data.charts.moviesByType,
                    userRegistrations: dashboardSummary.data.charts.userRegistrations,
                    bookingsByPaymentMethod: [
                        { name: "VNPAY", count: 45, revenue: 55000000 },
                        { name: "Momo", count: 35, revenue: 42000000 },
                        { name: "ZaloPay", count: 20, revenue: 28000000 },
                    ],
                },
                tables: {
                    topMovies: dashboardSummary.data.tables.topMovies.map((movie, index) => ({
                        id: index + 1,
                        title: movie.title,
                        genre: movie.genre,
                        rating: movie.rating,
                        totalBookings: movie.totalBookings,
                        revenue: movie.revenue,
                        poster: "https://cdn-icons-png.flaticon.com/512/4831/4831192.png",
                    })),
                    recentBookings: dashboardSummary.data.tables.recentBookings.map((booking, index) => ({
                        id: index + 1,
                        customerName: booking.fullName,
                        customerEmail: booking.email,
                        movieTitle: booking.movieTitle,
                        cinemaRoom: booking.cinemaRoomName,
                        bookingDate: booking.bookingDate,
                        seatCount: booking.seatCount,
                        totalAmount: booking.totalPrice,
                        paymentMethod: booking.paymentMethod,
                        status: booking.paymentStatus,
                    })),
                },
            };
            setDashboardData(mockData);
            setLoading(false);
        } catch (error) {
            if (error.message === "Network Error") {
                message.error("Không thể kết nối tới máy chủ. Vui lòng kiểm tra lại kết nối hoặc thử lại sau!");
            } else {
                message.error(`Đã xảy ra lỗi: ${error.message}. Vui lòng thử lại sau!`);
            }
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
                <Col xs={24} sm={12} lg={8}>
                    <Card className="hover:shadow-md hover:scale-102 transition-all duration-300">
                        <Statistic
                            title="Tổng Doanh Thu"
                            value={dashboardData.stats.totalRevenue}
                            precision={0}
                            valueStyle={{ color: "#3f8600" }}
                            prefix={<DollarSign size={20} />}
                            suffix="VNĐ"
                        />
                    </Card>
                </Col>
                <Col xs={24} lg={8}>
                    <Card className="hover:shadow-md hover:scale-102 transition-all duration-300">
                        <Statistic
                            title="Tổng Đặt Vé"
                            value={dashboardData.stats.totalBookings}
                            valueStyle={{ color: "#cf1322" }}
                            prefix={<Calendar size={20} />}
                        />
                    </Card>
                </Col>
                <Col xs={24} lg={8}>
                    <Card className="hover:shadow-md hover:scale-102 transition-all duration-300">
                        <Statistic
                            title="Tổng Thành Viên"
                            value={dashboardData.stats.totalUsers}
                            valueStyle={{ color: "#1890ff" }}
                            prefix={<Users size={20} />}
                        />
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]}>
                {/* <Col xs={24} sm={12} lg={6}>
                    <Card className="hover:shadow-md hover:scale-102 transition-all duration-300">
                        <Statistic
                            title="Tổng Đánh Giá"
                            value={dashboardData.stats.totalReviews}
                            valueStyle={{ color: "#fa8c16" }}
                            prefix={<Eye size={20} />}
                        />
                    </Card>
                </Col>
                <Col xs={24} sm={12} lg={6}>
                    <Card className="hover:shadow-md hover:scale-102 transition-all duration-300">
                        <Statistic
                            title="Đánh Giá TB"
                            value={dashboardData.stats.avgRating}
                            precision={1}
                            valueStyle={{ color: "#faad14" }}
                            prefix={<Star size={20} />}
                            suffix="/5"
                        />
                    </Card>
                </Col> */}
                <Col xs={24} lg={8}>
                    <Card className="hover:shadow-md hover:scale-102 transition-all duration-300">
                        <Statistic
                            title="Khuyến Mãi Hiện Tại"
                            value={dashboardData.stats.activePromotions}
                            valueStyle={{ color: "#52c41a" }}
                            prefix={<TrendingUp size={20} />}
                        />
                    </Card>
                </Col>
                <Col xs={24} lg={8}>
                    <Card className="hover:shadow-md hover:scale-102 transition-all duration-300">
                        <Statistic
                            title="Tổng Phòng Chiếu"
                            value={dashboardData.stats.totalCinemaRooms}
                            valueStyle={{ color: "#13c2c2" }}
                            prefix={<UserCheck size={20} />}
                        />
                    </Card>
                </Col>
                <Col xs={24} lg={8}>
                    <Card className="hover:shadow-md hover:scale-102 transition-all duration-300">
                        <Statistic
                            title="Phim Đang Chiếu"
                            value={dashboardData.stats.activeMovies}
                            valueStyle={{ color: "#722ed1" }}
                            prefix={<Film size={20} />}
                        />
                    </Card>
                </Col>
            </Row>

            {/* Charts */}
            <Row gutter={[16, 16]}>
                <Col xs={24} lg={16}>
                    <Card
                        className="hover:shadow-lg hover:scale-101 transition-all duration-300"
                        title="Biểu Đồ Doanh Thu & Đặt Vé"
                    >
                        <RevenueChart
                            data={dashboardData.charts.revenue[revenueTimeRange]}
                            timeRange={revenueTimeRange}
                            onTimeRangeChange={setRevenueTimeRange}
                        />
                    </Card>
                </Col>
                <Col xs={24} lg={8}>
                    <Card
                        className="hover:shadow-lg hover:scale-101 transition-all duration-300"
                        title="Phân Loại Phim Theo Thể Loại"
                    >
                        <MovieChart data={dashboardData.charts.moviesByType} />
                    </Card>
                </Col>
            </Row>

            <Row gutter={[16, 16]}>
                <Col xs={24} lg={12}>
                    <Card
                        className="hover:shadow-lg hover:scale-101 transition-all duration-300"
                        title="Thống Kê Đăng Ký Thành Viên"
                    >
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
