import React, { useEffect, useState } from "react";
import { Input, Alert, Button, Spin } from "antd";
import axios from "axios";
import axiosClient from '../../../../config/axios'

export default function EditTypeForm({ typeId, onSuccess, onCancel }) {
  const [form, setForm] = useState({
    name: "",
    loading: true,
    message: "",
    success: null,
    submitting: false,
  });
const token = localStorage.getItem('token')
  console.log(">>> Token: " + token)
  useEffect(() => {
    const fetchType = async () => {
      try {
        const data = await axiosClient.get(`/types/${typeId}`);
        setForm((f) => ({ ...f, name: data.name, loading: false }));
      } catch {
        setForm((f) => ({
          ...f,
          message: "Không thể tải thể loại!",
          success: false,
          loading: false,
        }));
      }
    };
    fetchType();
  }, [typeId]);

  const handleSubmit = async () => {
    if (!form.name.trim()) {
      return setForm((f) => ({
        ...f,
        message: "Tên thể loại không được để trống!",
        success: false,
      }));
    }

    setForm((f) => ({ ...f, submitting: true }));

    try {
      await axios.put(`http://localhost:8081/api/types/${typeId}`, {
        id: typeId,
        name: form.name,
      }, {headers: {
        "Authorization": `Bearer ${token}`,
        "Content-Type": "application/json"
      }});

      setForm((f) => ({
        ...f,
        message: "Cập nhật thành công!",
        success: true,
        submitting: false,
      }));

      onSuccess?.();
      onCancel?.(); // Có thể bỏ nếu muốn giữ lại modal
    } catch (err) {
      console.error("Lỗi khi cập nhật thể loại:", err);
      setForm((f) => ({
        ...f,
        message:
          "Cập nhật thất bại: " +
          (err.response?.data?.message || "Lỗi không xác định!"),
        success: false,
        submitting: false,
      }));
    }
  };

  if (form.loading) return <Spin />;

  return (
    <div>
      {form.message && (
        <Alert
          type={form.success === false ? "error" : "success"}
          message={form.message}
          className="mb-3"
        />
      )}

      <Input
        value={form.name}
        onChange={(e) => {
          const raw = e.target.value;
          const formatted =
            raw.charAt(0).toUpperCase() + raw.slice(1).toLowerCase();
          setForm((f) => ({ ...f, name: formatted }));
        }}
        onPressEnter={handleSubmit}
        placeholder="Nhập tên thể loại"
        size="large"
      />
      <div className="d-flex justify-content-end gap-2 mt-3">
        <Button onClick={onCancel}>Huỷ</Button>
        <Button
          type="primary"
          loading={form.submitting}
          onClick={handleSubmit}
        >
          {form.submitting ? "Đang lưu..." : "Cập nhật"}
        </Button>
      </div>
    </div>
  );
}
