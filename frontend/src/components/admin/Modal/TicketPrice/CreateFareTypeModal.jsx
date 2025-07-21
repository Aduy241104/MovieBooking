import { Col, Form, Input, Modal, notification, Row, Select } from "antd";
import { useState } from "react";
import { createFareTypeAPI } from "../../../../service/TicketPriceService";
import { v4 as uuidv4 } from "uuid";

export const CreateFareTypeModal = (props) => {
  const { isCreateModalOpen, setIsCreateModalOpen, setRefreshFlag } = props;
  const [form] = Form.useForm();
  const [timeSlotType, setTimeSlotType] = useState("");
  

  const handleSubmit = async (values) => {
    try {
      // Tạo fare_type_id tự động
      const fareTypeId = `FT-${uuidv4().slice(0, 8).toUpperCase()}`; // Ví dụ: FT-12345678

      const res = await createFareTypeAPI({
        ...values,
        fare_type_id: fareTypeId,
      });

      if (res.status === 201) {
        notification.success({
          message: "THÊM THÀNH CÔNG",
          description: `Thêm mới loại giá vé ${values.name} thành công`,
        });
        setRefreshFlag((prev) => !prev);
        setIsCreateModalOpen(false);
      } else {
        notification.error({
          message: "THÊM THẤT BẠI",
          description: `ERROR: ${res.message || "Unknown error"}`,
        });
      }
    } catch (err) {
      console.error("Error creating fare type:", err);
      notification.error({
        message: "THÊM THẤT BẠI",
        description: `ERROR: ${err.message || "Unknown error"}`,
      });
    }
  };

  return (
    <Modal
      title="THÊM LOẠI GIÁ VÉ"
      open={isCreateModalOpen}
      onOk={() => form.submit()}
      onCancel={() => setIsCreateModalOpen(false)}
      afterClose={() => {
        form.resetFields();
        setTimeSlotType("");
      }}
      okText="Thêm mới"
      cancelText="Hủy"
      maskClosable={false}
    >
      <Form
        form={form}
        layout="vertical"
        onFinish={handleSubmit}
        onValuesChange={(changedValues) => {
          if (changedValues.timeSlotType) {
            setTimeSlotType(changedValues.timeSlotType);
          }
        }}
      >
        {/* Giá cơ bản (VNĐ) */}
        <Form.Item
          label="Giá cơ bản (VNĐ)"
          name="basePrice"
          rules={[
            { required: true, message: "Vui lòng nhập giá cơ bản!" },
            {
              validator: (_, value) => {
                if (!value) return Promise.resolve();
                const num = Number(value);
                if (isNaN(num)) {
                  return Promise.reject(new Error("Giá cơ bản phải là số!"));
                }
                if (num <= 0) {
                  return Promise.reject(new Error("Giá cơ bản phải lớn hơn 0!"));
                }
                if (num > 1000000) {
                  return Promise.reject(
                    new Error("Giá cơ bản không được quá 1,000,000 VNĐ!")
                  );
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          <Input
            type="number"
            min={0}
            max={1000000}
            addonAfter="VNĐ"
            placeholder="VD: 80000"
          />
        </Form.Item>

        {/* Giá theo ngày (VNĐ) */}
        <Form.Item
          label="Giá theo ngày (VNĐ)"
          name="dayPrice"
          rules={[
            { required: true, message: "Vui lòng nhập giá theo ngày!" },
            {
              validator: (_, value) => {
                if (!value) return Promise.resolve();
                const num = Number(value);
                if (isNaN(num)) {
                  return Promise.reject(new Error("Giá theo ngày phải là số!"));
                }
                if (num < 0) {
                  return Promise.reject(
                    new Error("Giá theo ngày phải lớn hơn hoặc bằng 0!")
                  );
                }
                if (num > 1000000) {
                  return Promise.reject(
                    new Error("Giá theo ngày không được quá 1,000,000 VNĐ!")
                  );
                }
                return Promise.resolve();
              },
            },
          ]}
        >
          <Input
            type="number"
            min={0}
            max={1000000}
            addonAfter="VNĐ"
            placeholder="VD: 100000"
          />
        </Form.Item>

        <Row justify="space-between">
          <Col span={11}>
            {/* Tên loại giá vé */}
            <Form.Item
              label="Lồng tiếng / Phụ đề"
              name="name"
              rules={[{ required: true, message: "Vui lòng chọn Lồng tiếng / Phụ đề!" }]}
            >
              <Select
                placeholder="Chọn Lồng tiếng / Phụ đề"
                options={[
                  { value: "Vietsub", label: "Vietsub" },
                  { value: "Lồng Tiếng", label: "Lồng tiếng" },
                ]}
              />
            </Form.Item>
          </Col>

          <Col span={11}>
            {/* Định dạng phim */}
            <Form.Item
              label="Định dạng phim"
              name="movieFormat"
              rules={[{ required: true, message: "Vui lòng chọn định dạng phim!" }]}
            >
              <Select
                placeholder="Chọn định dạng phim"
                options={[
                  { value: "2D", label: "2D" },
                  { value: "3D", label: "3D" },
                  { value: "IMAX", label: "IMAX" },
                ]}
              />
            </Form.Item>
          </Col>
        </Row>

        <Form.Item
          label="Khung giờ"
          name="timeSlotType"
          rules={[{ required: true, message: "Vui lòng chọn khung giờ!" }]}
        >
         <Select
            placeholder="Chọn khung giờ"
            options={[
              { value: "Ngày Thường", label: "Ngày Thường" },
              { value: "Ngày Lễ", label: "Ngày Lễ" },
              { value: "Ngày Cuối Tuần", label: "Ngày Cuối Tuần" },
              { value: "Xuất Chiếu Sớm", label: "Xuất Chiếu Sớm" },
            ]}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};