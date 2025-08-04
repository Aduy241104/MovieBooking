import React, { useState, useEffect } from 'react';
import { Button, DatePicker, message, Space, Typography } from 'antd';
import { FileSpreadsheet } from 'lucide-react';
import dayjs from 'dayjs';
import 'dayjs/locale/vi';
import weekday from 'dayjs/plugin/weekday';
import localeData from 'dayjs/plugin/localeData';
import axiosInstance from '../../config/axiosBooking'; 
import { useLocation, useOutletContext } from "react-router-dom";

dayjs.extend(weekday);
dayjs.extend(localeData);
dayjs.locale('vi');

const { RangePicker } = DatePicker;
const { Title, Paragraph } = Typography;

const ExportExcelPage = () => {
    const location = useLocation();
    const { setBreadcrumbItems } = useOutletContext();
    useEffect(() => {
        if (location.pathname.includes("/admin/export-reports")) {
            setBreadcrumbItems([
                { title: "Trang chủ", href: "/admin" },
                { title: "Xuất file" },
            ]);
        }
    }, [location.pathname, setBreadcrumbItems]);

    const [dates, setDates] = useState([dayjs(), dayjs()]);
    const [isExporting, setIsExporting] = useState(false);

    const handleDateChange = (dates) => {
        if (dates) {
            setDates(dates);
        }
    };

    const handleExport = async () => {
        if (!dates || dates.length !== 2) {
            message.error("Vui lòng chọn khoảng thời gian hợp lệ.");
            return;
        }

        setIsExporting(true);
        message.loading({ content: 'Đang chuẩn bị file...', key: 'exporting' });

        const startDate = dates[0].format('YYYY-MM-DD');
        const endDate = dates[1].format('YYYY-MM-DD');

        try {
            const response = await axiosInstance.get('/admin/export/excel', {
                params: { startDate, endDate },
                responseType: 'blob',
            });

    //Create blob
            const blob = new Blob([response.data], { type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet' });
            
            // CreateL object from Blob
            const url = window.URL.createObjectURL(blob);
            const link = document.createElement('a');
            link.href = url;
    
            const contentDisposition = response.headers['content-disposition'];
            let fileName = `bookings_${startDate}_to_${endDate}.xlsx`;
            if (contentDisposition) {
                const fileNameMatch = contentDisposition.match(/filename="(.+)"/);
                if (fileNameMatch && fileNameMatch.length === 2) {
                    fileName = fileNameMatch[1];
                }
            }
            
            link.setAttribute('download', fileName);
            document.body.appendChild(link);
            link.click();
            
            // Clean
            document.body.removeChild(link);
            window.URL.revokeObjectURL(url);

            message.success({ content: 'Tải file thành công!', key: 'exporting', duration: 2 });
        } catch (error) {
            console.error("Error exporting to Excel:", error);
            // Read error from blob
            if (error.response && error.response.data instanceof Blob && error.response.data.type.includes("application/json")) {
                 const reader = new FileReader();
                 reader.onload = function() {
                     const errorJson = JSON.parse(this.result);
                     message.error({ content: errorJson.message || 'Xuất file thất bại!', key: 'exporting', duration: 2 });
                 };
                 reader.readAsText(error.response.data);
            } else {
                 message.error({ content: 'Xuất file thất bại! Kiểm tra lại kết nối hoặc quyền truy cập.', key: 'exporting', duration: 2 });
            }
        } finally {
            setIsExporting(false);
        }
    };

    return (
        <div style={{ padding: '24px', background: '#fff', borderRadius: '8px', maxWidth: '600px', margin: '0 auto' }}>
            <Title level={3}>Xuất Báo Cáo Booking</Title>
            <Paragraph type="secondary">
                Chọn khoảng thời gian bạn muốn xuất báo cáo. Hệ thống sẽ tạo một file Excel chứa tất cả các lượt đặt vé đã thành công trong khoảng thời gian đã chọn.
            </Paragraph>

            <Space direction="vertical" size="large" style={{ marginTop: '24px', width: '100%' }}>
                <RangePicker
                    value={dates}
                    onChange={handleDateChange}
                    style={{ width: '100%' }}
                    format="DD/MM/YYYY"
                />

                <Button
                    type="primary"
                    icon={<FileSpreadsheet size={16} style={{ marginRight: 8 }} />}
                    onClick={handleExport}
                    loading={isExporting}
                    block
                >
                    Xuất File Excel
                </Button>
            </Space>
        </div>
    );
};

export default ExportExcelPage;