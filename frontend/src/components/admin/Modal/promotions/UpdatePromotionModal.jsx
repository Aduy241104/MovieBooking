import { Col, Form, Input, Modal, notification, Row, Select, DatePicker } from "antd";
import dayjs from "dayjs";
import isBetween from "dayjs/plugin/isBetween";
import { DollarSign, Percent } from "lucide-react";
import { updatePromotionAPI } from "../../../../service/PromotionService";
import { useState, useEffect } from "react";

const { RangePicker } = DatePicker;
dayjs.extend(isBetween);

export const UpdatePromotionModal = (props) => {
    const { promotionText, isUpdateModalOpen, setIsUpdateModalOpen, setRefreshFlag, dataPromotion } = props;
    const [form] = Form.useForm();
    const [discountType, setDiscountType] = useState("");

    // Set initial values khi modal mở
    useEffect(() => {
        if (isUpdateModalOpen && dataPromotion) {
            const initialValues = {
                code: dataPromotion.code,
                discountType: dataPromotion.discountType ? dataPromotion.discountType.toLowerCase() : "",
                discountLevel: dataPromotion.discountLevel,
                maxDiscount: dataPromotion.maxDiscount,
                minOrder: dataPromotion.minOrder,
                dateRange:
                    dataPromotion.startTime && dataPromotion.endTime
                        ? [dayjs(dataPromotion.startTime), dayjs(dataPromotion.endTime)]
                        : null,
            };
            form.setFieldsValue(initialValues);
            setDiscountType(dataPromotion.discountType ? dataPromotion.discountType.toLowerCase() : "");
        }
    }, [isUpdateModalOpen, dataPromotion, form]);

    const handleSubmit = async (values) => {
        // console.log('Update Values: ', values);
        const now = dayjs();
        const [startTime, endTime] = values.dateRange;

        const res = await updatePromotionAPI(dataPromotion.id, {
            ...values,
            startTime: startTime ? dayjs(startTime).format("YYYY-MM-DDTHH:mm:ss") : null,
            endTime: endTime ? dayjs(endTime).format("YYYY-MM-DDTHH:mm:ss") : null,
            active: now.isBetween(startTime, endTime) ? true : false,
        });

        if (res.result) {
            notification.success({
                message: "CẬP NHẬT THÀNH CÔNG",
                description: `Cập nhật ${promotionText.toLowerCase()} thành công`,
            });
            setRefreshFlag((prev) => !prev);
            setIsUpdateModalOpen(false);
            return;
        }
        notification.error({
            message: "CẬP NHẬT THẤT BẠI",
            description: `ERROR: ${res.message}`,
        });
    };

    return (
        <>
            <Modal
                title={`CẬP NHẬT ${promotionText.toUpperCase()}`}
                open={isUpdateModalOpen}
                onOk={() => form.submit()}
                onCancel={() => setIsUpdateModalOpen(false)}
                afterClose={() => {
                    form.resetFields();
                    setDiscountType("");
                }}
                okText={"Cập nhật"}
                cancelText={"Huỷ"}
                maskClosable={false}
            >
                <Form
                    form={form}
                    layout="vertical"
                    onFinish={handleSubmit}
                    onValuesChange={(changedValues) => {
                        if (changedValues.discountType) {
                            setDiscountType(changedValues.discountType);
                        }
                    }}
                >
                    <Form.Item
                        label={`${promotionText}`}
                        name="code"
                        rules={[
                            { required: true, message: "Vui lòng nhập mã khuyến mãi!" },
                            { min: 3, message: "Mã khuyến mãi phải có ít nhất 3 ký tự!" },
                            { max: 20, message: "Mã khuyến mãi không được quá 20 ký tự!" },
                            {
                                pattern: /^[a-zA-Z0-9]+$/,
                                message: "Mã khuyến mãi chỉ được chứa chữ hoa và số!",
                            },
                        ]}
                    >
                        <Input
                            placeholder="VD: SUMMER2024, GIAMGIA50"
                            style={{ textTransform: "uppercase" }}
                            onChange={(e) => {
                                e.target.value = e.target.value.toUpperCase();
                            }}
                        />
                    </Form.Item>

                    <Row justify={"space-between"}>
                        <Col lg={8}>
                            <Form.Item
                                label="Hình thức giảm"
                                name="discountType"
                                rules={[{ required: true, message: "Vui lòng chọn hình thức giảm!" }]}
                            >
                                <Select
                                    options={[
                                        {
                                            value: "percent",
                                            label: (
                                                <div className="flex items-center gap-1">
                                                    <Percent color="#cf075e" size={16} strokeWidth={1.5} />
                                                    <p>Phần trăm</p>
                                                </div>
                                            ),
                                        },
                                        {
                                            value: "amount",
                                            label: (
                                                <div className="flex items-center gap-1">
                                                    <DollarSign color="#106511" size={16} strokeWidth={1.5} />
                                                    <p>Số tiền</p>
                                                </div>
                                            ),
                                        },
                                    ]}
                                    placeholder="Chọn loại khuyến mãi"
                                />
                            </Form.Item>
                        </Col>

                        <Col lg={14}>
                            <Form.Item
                                label="Giá trị giảm"
                                name="discountLevel"
                                rules={[
                                    { required: true, message: "Vui lòng nhập giá trị giảm!" },
                                    {
                                        validator: (_, value) => {
                                            if (!value) return Promise.resolve();

                                            const num = Number(value);
                                            if (isNaN(num)) {
                                                return Promise.reject(new Error("Giá trị giảm phải là số!"));
                                            }

                                            if (num <= 0) {
                                                return Promise.reject(new Error("Giá trị giảm phải lớn hơn 0!"));
                                            }

                                            if (discountType === "percent") {
                                                if (num > 99) {
                                                    return Promise.reject(
                                                        new Error("Phần trăm giảm không được quá 99%!")
                                                    );
                                                }
                                            } else if (discountType === "amount") {
                                                if (num > 9999000) {
                                                    return Promise.reject(
                                                        new Error("Số tiền giảm không được quá 9,999,000 VNĐ!")
                                                    );
                                                }
                                            }

                                            return Promise.resolve();
                                        },
                                    },
                                ]}
                            >
                                <Input
                                    type="number"
                                    min={0}
                                    max={discountType === "percent" ? 99 : 9999000}
                                    addonAfter={discountType === "percent" ? "%" : "VNĐ"}
                                    placeholder={discountType === "percent" ? "VD: 15" : "VD: 50000"}
                                />
                            </Form.Item>
                        </Col>
                    </Row>

                    <Form.Item
                        label="Giảm tối đa (VNĐ)"
                        name="maxDiscount"
                        rules={[
                            {
                                validator: (_, value) => {
                                    // Nếu là giảm theo số tiền, không cần validate
                                    if (discountType === "amount") {
                                        return Promise.resolve();
                                    }

                                    // Nếu là giảm theo %, phải nhập giảm tối đa
                                    if (discountType === "percent" && !value) {
                                        return Promise.reject(
                                            new Error("Vui lòng nhập giảm tối đa khi giảm theo phần trăm!")
                                        );
                                    }

                                    if (value) {
                                        const num = Number(value);
                                        if (isNaN(num)) {
                                            return Promise.reject(new Error("Giảm tối đa phải là số!"));
                                        }

                                        if (num <= 0) {
                                            return Promise.reject(new Error("Giảm tối đa phải lớn hơn 0!"));
                                        }

                                        if (num > 9999000) {
                                            return Promise.reject(
                                                new Error("Giảm tối đa không được quá 9,999,000 VNĐ!")
                                            );
                                        }
                                    }

                                    return Promise.resolve();
                                },
                            },
                        ]}
                    >
                        <Input
                            type="number"
                            min={0}
                            max={50000000}
                            addonAfter="VNĐ"
                            placeholder="VD: 100000"
                            disabled={discountType === "amount"}
                            style={{
                                backgroundColor: discountType === "amount" ? "#f5f5f5" : "white",
                            }}
                        />
                    </Form.Item>

                    <Form.Item
                        label="Ngưỡng áp dụng (VNĐ)"
                        name="minOrder"
                        rules={[
                            { required: true, message: "Vui lòng nhập ngưỡng áp dụng!" },
                            {
                                validator: (_, value) => {
                                    if (!value) return Promise.resolve();

                                    const num = Number(value);
                                    if (isNaN(num)) {
                                        return Promise.reject(new Error("Ngưỡng áp dụng phải là số!"));
                                    }

                                    if (num < 0) {
                                        return Promise.reject(new Error("Ngưỡng áp dụng không được âm!"));
                                    }

                                    if (num > 9999000) {
                                        return Promise.reject(
                                            new Error("Ngưỡng áp dụng không được quá 9,999,000 VNĐ!")
                                        );
                                    }

                                    // Kiểm tra ngưỡng áp dụng phải lớn hơn giảm tối đa
                                    const maxDiscount = form.getFieldValue("maxDiscount");
                                    if (maxDiscount && num < Number(maxDiscount)) {
                                        return Promise.reject(
                                            new Error("Ngưỡng áp dụng phải lớn hơn hoặc bằng giảm tối đa!")
                                        );
                                    }
                                    const discountLevel = form.getFieldValue("discountLevel");
                                    if (discountLevel && num < Number(discountLevel)) {
                                        return Promise.reject(
                                            new Error("Ngưỡng áp dụng phải lớn hơn hoặc bằng giá trị giảm!")
                                        );
                                    }

                                    return Promise.resolve();
                                },
                            },
                        ]}
                    >
                        <Input type="number" min={0} max={9999000} addonAfter="VNĐ" placeholder="VD: 200000" />
                    </Form.Item>

                    <Form.Item
                        label="Thời gian áp dụng"
                        name="dateRange"
                        rules={[
                            { required: true, message: "Vui lòng chọn thời gian áp dụng!" },
                            {
                                validator: (_, value) => {
                                    if (!value || !value[0] || !value[1]) {
                                        return Promise.resolve();
                                    }

                                    const [startTime, endTime] = value;
                                    const now = dayjs();

                                    // Cho phép update nếu thời gian bắt đầu đã qua (đang hoạt động)
                                    // nhưng thời gian kết thúc phải trong tương lai
                                    if (endTime.isBefore(now, "minute")) {
                                        return Promise.reject(
                                            new Error("Thời gian kết thúc không được trong quá khứ!")
                                        );
                                    }

                                    // Kiểm tra thời gian kết thúc phải sau thời gian bắt đầu ít nhất 1 giờ
                                    if (endTime.diff(startTime, "hour") < 1) {
                                        return Promise.reject(
                                            new Error("Thời gian kết thúc phải sau thời gian bắt đầu ít nhất 1 giờ!")
                                        );
                                    }

                                    // Kiểm tra thời gian áp dụng không quá 1 năm
                                    if (endTime.diff(startTime, "year") > 1) {
                                        return Promise.reject(new Error("Thời gian áp dụng không được quá 1 năm!"));
                                    }

                                    return Promise.resolve();
                                },
                            },
                        ]}
                    >
                        <RangePicker
                            showTime
                            format="DD/MM/YYYY HH:mm"
                            placeholder={["Ngày bắt đầu", "Ngày kết thúc"]}
                            style={{ width: "100%" }}
                            disabledDate={(current) => {
                                // Cho phép chọn ngày trong quá khứ để update
                                return false;
                            }}
                        />
                    </Form.Item>
                </Form>
            </Modal>
        </>
    );
};
