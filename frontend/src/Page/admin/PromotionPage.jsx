import { SearchOutlined } from '@ant-design/icons';
import { Button, Input, Select } from "antd";
import { useEffect, useState } from "react";
import { useOutletContext } from "react-router-dom";
import { CreatePromotionModal } from '../../components/admin/Modal/promotions/CreatePromotionModal';
import { PromotionTable } from '../../components/admin/Table/PromotionTable';
import { fetchAllPromotionAPI, updatePromotionActiveAPI } from '../../service/PromotionService';
import { TicketPlus } from 'lucide-react';
import { debounce } from 'lodash';
import { sfAnd, sfEqual, sfLike, sfOr } from 'spring-filter-query-builder';
import dayjs from 'dayjs';


export const PromotionPage = (props) => {
    const { setBreadcrumbItems } = useOutletContext();
    const { promotionText } = props;

    const [dataPromotions, setDataPromotions] = useState(null);
    const [searchPromotion, setSearchPromotion] = useState("");
    const [promotionActive, setPromotionActive] = useState(null);
    const [isCreateModalOpen, setIsCreateModalOpen] = useState(false);
    const [refreshFlag, setRefreshFlag] = useState(false);
    const [page, setPage] = useState(1);
    const [size, setSize] = useState(10);
    const [total, setTotal] = useState(0);
    const [filter, setFilter] = useState("");

    useEffect(() => {
        setBreadcrumbItems([
            { title: 'Trang chủ', href: '/admin' },
            { title: `${promotionText}` },
        ]);
    }, []);

    // Hàm kiểm tra và cập nhật các promotion đã hết hạn
    const checkAndUpdateExpiredPromotions = async (promotions) => {
        const now = dayjs();
        const updatedPromotions = [...promotions];
        let hasUpdates = false;

        for (let i = 0; i < updatedPromotions.length; i++) {
            const promotion = updatedPromotions[i];

            // Kiểm tra promotion hết hạn
            if (promotion.active && promotion.endTime) {
                const endTime = dayjs(promotion.endTime);
                if (now.isAfter(endTime)) {
                    try {
                        // Update trong database
                        await updatePromotionActiveAPI(promotion.id, false);

                        // Update local state
                        updatedPromotions[i] = { ...promotion, active: false };
                        hasUpdates = true;

                        console.log(`Auto disabled expired promotion: ${promotion.code}`);
                    } catch (error) {
                        console.error(`Error updating promotion ${promotion.code}:`, error);
                    }
                }
            }
        }

        return { updatedPromotions, hasUpdates };
    };

    useEffect(() => {
        const loadPromotions = async () => {
            try {
                const res = await fetchAllPromotionAPI(page, size, filter);
                if (res && res.result) {
                    setTotal(res.result.meta.total);

                    // Check và update expired promotions
                    const { updatedPromotions, hasUpdates } = await checkAndUpdateExpiredPromotions(res.result.data);
                    setDataPromotions(updatedPromotions);

                    if (hasUpdates) {
                        // Có thể show notification
                        console.log('Some expired promotions have been automatically disabled');
                    }
                }
            } catch (error) {
                console.error('Error loading promotions:', error);
            }
        }

        loadPromotions();
    }, [page, size, filter, refreshFlag]);

    useEffect(() => {
        const handleFilterPromotion = debounce(() => {
            const filters = [];
            const searchTerm = searchPromotion ? searchPromotion.toUpperCase() : "";
            if (searchPromotion) {
                filters.push(
                    sfOr([
                        sfLike(('code'), `*${searchTerm}*`),
                    ])
                );
            }
            if (promotionActive !== null && promotionActive !== undefined) {
                filters.push(sfEqual('active', promotionActive));
            }
            // Chỉ set filter khi có filters
            if (filters.length > 0) {
                const filter = sfAnd(filters);
                setFilter(filter.toString());
            } else {
                setFilter(""); // Reset filter về rỗng
            }
        }, 500);

        // Chỉ gọi khi thực sự có search hoặc filter
        if (searchPromotion || promotionActive !== null) {
            handleFilterPromotion();
        }

        return () => handleFilterPromotion.cancel();
    }, [searchPromotion, promotionActive]);


    return (
        <>
            <div style={{
                padding: 24,
                // minHeight: 360,
                background: '#fff',
                borderRadius: '8px',
                boxShadow: '0 2px 8px rgba(0,0,0,0.1)',
            }}>
                <div className='flex justify-between mb-4'>
                    <div style={{ display: "flex", gap: "2rem" }}>
                        <Input style={{ width: "30vw" }}
                            size='large'
                            addonBefore={<SearchOutlined />}
                            placeholder="Tìm kiếm khuyến mãi..."
                            allowClear
                            onChange={(value) => setSearchPromotion(value.target.value)}
                        />
                        <Select
                            size='large'
                            style={{ width: "10vw" }}
                            options={[
                                { value: true, label: 'Hiệu lực' },
                                { value: false, label: 'Vô hiệu' }
                            ]}
                            placeholder="Trạng thái"
                            allowClear
                            onChange={(value) => setPromotionActive(value)}
                        />
                    </div>
                    <Button onClick={() => setIsCreateModalOpen(true)}
                        size='large'
                        type="primary"
                    >
                        <TicketPlus size={20} strokeWidth={1.5} />
                        <span>Thêm {promotionText}</span>
                    </Button>
                </div>

                <PromotionTable
                    page={page} setPage={setPage} size={size} total={total}
                    dataPromotions={dataPromotions}
                    promotionText={promotionText}
                    setRefreshFlag={setRefreshFlag}
                />

            </div>

            <CreatePromotionModal
                isCreateModalOpen={isCreateModalOpen}
                setIsCreateModalOpen={setIsCreateModalOpen}
                promotionText={promotionText}
                setRefreshFlag={setRefreshFlag}
            />
        </>

    );
}