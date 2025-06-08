import { message, Pagination, Popconfirm, Space, Table, Tag } from "antd";
import dayjs from "dayjs";
import { Lock, LockOpen, SquarePen, Trash2 } from "lucide-react";
import { useState } from "react";
import { UpdatePromotionModal } from "../Modal/promotions/UpdatePromotionModal";
import { deletePromotionAPI, updatePromotionActiveAPI } from "../../../service/PromotionService";



export const PromotionTable = (props) => {

    const { page, size, setPage, total, dataPromotions, promotionText, setRefreshFlag } = props;
    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false);
    const [dataPromotion, setDataPromotions] = useState("");

    const handleTableChange = (pagination) => {
        if (pagination)
            setPage(pagination)
    }

    const handleUpdatePromotionActive = async (record) => {
        const res = await updatePromotionActiveAPI(record.id, record.active === false ? true : false)
        if (res.result) {
            message.success(
                <span>
                    {record.active === false ? 'Khôi phục mã khuyến mãi ' : 'Vô hiệu mã khuyến mãi '}
                    <span className='font-medium'>{record.code}</span>
                    {' thành công'}
                </span>
            );
            setRefreshFlag(prev => !prev);
            return;
        }
        message.error(`Error: ${res.message}`)
    };

    const handleDeletePromotion = async (record) => {
        const res = await deletePromotionAPI(record.id);
        if (res.result) {
            message.success(`Mã ${record.code} đã được xoá thành công`);
            setRefreshFlag(prev => !prev);
            return;
        }
        message.error(`Error: ${res.message}`)
    };

    const columns = [
        {
            title: 'STT',
            render: (_, render, index) => (
                <>
                    {(page - 1) * size + index + 1}
                </>
            ),
        },
        {
            title: 'Mã khuyến mãi',
            dataIndex: 'code',
        },
        {
            title: 'Giá trị giảm',
            dataIndex: 'discountLevel',
            render: (text, record) => (
                <>
                    {text ? (record.discountType === 'percent' ? `${text}%` : Number(text).toLocaleString('vi-VN', { style: 'currency', currency: 'VND' }))
                        : "Không có dữ liệu"}
                </>
            ),
        },
        {
            title: 'Bắt đầu',
            dataIndex: 'startTime',
            render: (text) => (
                <>
                    {text ? dayjs(text).format("DD/MM/YYYY HH:mm A") : "Không có dữ liệu"}
                </>
            ),
        },
        {
            title: 'Hết hạn',
            dataIndex: 'endTime',
            render: (text) => (
                <>
                    {text ? dayjs(text).format("DD/MM/YYYY HH:mm A") : "Không có dữ liệu"}
                </>
            ),
        },
        {
            title: 'Trạng thái',
            dataIndex: 'active',
            render: (text) => (
                <>
                    <Tag color={text === false ? "volcano" : "green"}>
                        {text === false ? "Vô hiệu" : "Hiệu lực"}
                    </Tag>
                </>
            ),
        },
        {
            title: 'Hành động',
            render: (_, record) => (
                <>
                    <Space size="large">
                        <button className="text-blue-600 hover:text-fuchsia-500" onClick={() => {
                            setIsUpdateModalOpen(true);
                            setDataPromotions(record);

                        }}>
                            <SquarePen size={16} strokeWidth={1.7} />
                        </button>

                        {record.active === false ? (
                            <>
                                <Popconfirm
                                    placement="left"
                                    title="Khôi phục mã khuyến mãi"
                                    description="Xác nhận khôi phục?"
                                    onConfirm={() => handleUpdatePromotionActive(record)}
                                    okText="Xác nhận"
                                    cancelText="Huỷ"
                                >
                                    <button style={{ color: "green" }}>
                                        <LockOpen size={16} strokeWidth={1.7} />
                                    </button>
                                </Popconfirm>
                            </>
                        ) : (
                            <>
                                <Popconfirm
                                    placement="left"
                                    title="Vô hiệu mã khuyến mãi"
                                    description="Xác nhận vô hiệu?"
                                    onConfirm={() => handleUpdatePromotionActive(record)}
                                    okText="Xác nhận"
                                    cancelText="Huỷ"
                                >
                                    <button style={{ color: "red" }}>
                                        <Lock size={16} strokeWidth={1.7} />
                                    </button>
                                </Popconfirm>
                            </>
                        )}

                        <Popconfirm
                            placement="left"
                            title="Xoá mã khuyến mãi"
                            description="Xác nhận xoá?"
                            onConfirm={() => handleDeletePromotion(record)}
                            okText="Xoá"
                            cancelText="Huỷ"
                        >
                            <button className="text-amber-600 hover:text-amber-700">
                                <Trash2 size={16} strokeWidth={1.7} />
                            </button>
                        </Popconfirm>

                    </Space>
                </>

            ),
        },
    ];

    return (
        <>
            <Table
                columns={columns}
                dataSource={dataPromotions}
                rowKey={"id"}
                pagination={false}
            />

            <div className="flex justify-center mt-4">
                <Pagination
                    current={page}
                    pageSize={size}
                    total={total}
                    showTotal={(total, range) => `${range[0]}-${range[1]} trong ${total} mục`}
                    onChange={handleTableChange}
                />
            </div>

            <UpdatePromotionModal
                isUpdateModalOpen={isUpdateModalOpen}
                setIsUpdateModalOpen={setIsUpdateModalOpen}
                dataPromotion={dataPromotion}
                setDataPromotion={setDataPromotions}
                setRefreshFlag={setRefreshFlag}
                promotionText={promotionText}
            />
        </>
    );
}