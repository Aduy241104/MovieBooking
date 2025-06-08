import React from 'react';
import {
    AreaChart,
    Area,
    XAxis,
    YAxis,
    CartesianGrid,
    Tooltip,
    ResponsiveContainer
} from 'recharts';

export const UserChart = ({ data }) => {
    return (
        <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer>
                <AreaChart data={data} margin={{ top: 10, right: 30, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip />
                    <Area 
                        type="monotone" 
                        dataKey="totalUsers" 
                        stackId="1" 
                        stroke="#8884d8" 
                        fill="#8884d8" 
                        name="Tổng người dùng"
                    />
                    <Area 
                        type="monotone" 
                        dataKey="newUsers" 
                        stackId="2" 
                        stroke="#82ca9d" 
                        fill="#82ca9d" 
                        name="Người dùng mới"
                    />
                </AreaChart>
            </ResponsiveContainer>
        </div>
    );
};