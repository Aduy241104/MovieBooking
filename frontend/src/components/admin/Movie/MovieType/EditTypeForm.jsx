import React, { useEffect, useState } from "react";
import { Input, Alert, Button, Spin } from "antd";
import axiosClient from '../../../../config/axios'
export default function EditTypeForm({ typeId, onSuccess, onCancel }) {
  const [form, setForm] = useState({ name: "", loading: true, message: "", success: null, submitting: false });

  useEffect(() => {
    const fetchType = async () => {
      try {
        const data = await axiosClient.get(`/public/types/${typeId}`);
        setForm(f => ({ ...f, name: data.name, loading: false }));
      } catch {
        setForm(f => ({ ...f, message: "Không thể tải thể loại!", success: false, loading: false }));
      }
    };
    fetchType();
  }, [typeId]);

  const handleSubmit = async () => {
    if (!form.name.trim()) return;
    setForm(f => ({ ...f, submitting: true }));
    try {
      await axiosClient.put(`/public/types/${typeId}`, { id: typeId, name: form.name });
      onSuccess?.();
      onCancel?.();
    } catch (err) {
      setForm(f => ({
        ...f,
        message: "Cập nhật thất bại: " + (err.response?.data?.message || "Lỗi không xác định!"),
        success: false,
        submitting: false
      }));
    }
  };

  if (form.loading) return <Spin />;

  return (
    <div>
      {form.message && <Alert type={form.success ? "success" : "error"} message={form.message} className="mb-3" />}
      <Input
        value={form.name}
        onChange={(e) => setForm(f => ({ ...f, name: e.target.value }))}
        placeholder="Nhập tên thể loại"
        size="large"
      />
      <div className="d-flex justify-content-end gap-2 mt-3">
        <Button onClick={onCancel}>Huỷ</Button>
        <Button type="primary" loading={form.submitting} onClick={handleSubmit}>
          {form.submitting ? "Đang lưu..." : "Cập nhật"}
        </Button>
      </div>
    </div>
  );
}
