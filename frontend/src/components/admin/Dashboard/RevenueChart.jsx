import React from "react";
import { Button, Space } from "antd";
import { ComposedChart, Line, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from "recharts";
import dayjs from "dayjs";

export const RevenueChart = ({ data, timeRange, onTimeRangeChange }) => {
    const formatCurrency = (value) => {
        return new Intl.NumberFormat("vi-VN", {
            style: "currency",
            currency: "VND",
        }).format(value);
    };

    const formatYAxis = (value) => {
        if (value >= 1000000) {
            return `${(value / 1000000).toFixed(0)}M`;
        }
        if (value >= 1000) {
            return `${(value / 1000).toFixed(0)}K`;
        }
        return value;
    };

    const timeRangeOptions = [
        { key: "day", label: "Theo ngày", icon: "📅" },
        { key: "week", label: "Theo tuần", icon: "📊" },
        { key: "month", label: "Theo tháng", icon: "📈" },
    ];

    return (
        <div>
            {/* Time Range Toggle Buttons */}
            <div className="mb-4 flex justify-between items-center">
                <Space>
                    {timeRangeOptions.map((option) => (
                        <Button
                            key={option.key}
                            type={timeRange === option.key ? "primary" : "default"}
                            size="small"
                            onClick={() => onTimeRangeChange(option.key)}
                            className="flex items-center gap-1"
                        >
                            <span>{option.icon}</span>
                            {option.label}
                        </Button>
                    ))}
                </Space>
                <div className="text-sm text-gray-500">
                    {timeRange === "day" && "Doanh thu 7 ngày gần nhất"}
                    {timeRange === "week" && "Doanh thu 6 tuần gần nhất"}
                    {timeRange === "month" && "Doanh thu 6 tháng gần nhất"}
                </div>
            </div>

            {/* Chart */}
            <div style={{ width: "100%", height: 400 }}>
                <ResponsiveContainer>
                    <ComposedChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis
                            dataKey="date"
                            fontSize={12}
                            tickFormatter={(value) => {
                                if (timeRange === "day") {
                                    return dayjs(value).format("DD/MM");
                                }
                                if (timeRange === "week") {
                                    const startOfWeek = dayjs(value);
                                    return `${startOfWeek.format("DD/MM")} - ${startOfWeek
                                        .add(6, "day")
                                        .format("DD/MM")}`;
                                }
                                if (timeRange === "month") {
                                    return dayjs(value).format("MM/YYYY");
                                }
                            }}
                        />
                        <YAxis yAxisId="left" orientation="left" tickFormatter={formatYAxis} fontSize={12} />
                        <YAxis yAxisId="right" orientation="right" fontSize={12} allowDecimals={false} />
                        <Tooltip
                            formatter={(value, name) => {
                                if (name === "Doanh thu") {
                                    return [formatCurrency(value), "Doanh thu"];
                                }
                                return [value, "Số vé"];
                            }}
                            labelFormatter={(label) => {
                                if (timeRange === "day") {
                                    return `${dayjs(label).format("DD/MM/YYYY")}`;
                                }
                                if (timeRange === "week") {
                                    const startOfWeek = dayjs(label);
                                    const endOfWeek = startOfWeek.add(6, "day");
                                    return `${startOfWeek.format("DD/MM/YYYY")} - ${endOfWeek.format("DD/MM/YYYY")}`;
                                }
                                if (timeRange === "month") {
                                    return `${dayjs(label).format("MM/YYYY")}`;
                                }
                            }}
                            labelStyle={{ color: "#000" }}
                            contentStyle={{
                                backgroundColor: "#fff",
                                border: "1px solid #ccc",
                                borderRadius: "6px",
                            }}
                        />
                        <Legend />
                        <Bar yAxisId="left" dataKey="revenue" fill="#8884d8" name="Doanh thu" radius={[4, 4, 0, 0]} />
                        <Line
                            yAxisId="right"
                            type="monotone"
                            dataKey="tickets"
                            stroke="#82ca9d"
                            strokeWidth={3}
                            name="Số vé"
                            dot={{ fill: "#82ca9d", strokeWidth: 2, r: 4 }}
                            activeDot={{ r: 6, stroke: "#82ca9d", strokeWidth: 2 }}
                        />
                    </ComposedChart>
                </ResponsiveContainer>
            </div>

            {/* Summary Stats */}
            <div className="mt-4 grid grid-cols-2 gap-4 text-center">
                <div className="bg-blue-50 p-3 rounded-lg">
                    <div className="text-lg font-semibold text-blue-600">
                        {formatCurrency(data.reduce((sum, item) => sum + item.revenue, 0))}
                    </div>
                    <div className="text-sm text-gray-600">Tổng doanh thu</div>
                </div>
                <div className="bg-green-50 p-3 rounded-lg">
                    <div className="text-lg font-semibold text-green-600">
                        {data.reduce((sum, item) => sum + item.tickets, 0).toLocaleString()}
                    </div>
                    <div className="text-sm text-gray-600">Tổng vé bán</div>
                </div>
            </div>
        </div>
    );
};
