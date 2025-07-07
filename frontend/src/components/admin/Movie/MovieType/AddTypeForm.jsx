import React, { useState } from "react";
import { Input, Alert, Button } from "antd";
import axiosClient from '../../../../config/axios';

export default function AddTypeForm({ onTypeAdded, onSuccessClose }) {
  const [form, setForm] = useState({
    name: "",
    message: "",
    success: null,
    submitting: false
  });

  const handleAdd = async (shouldClose = false) => {
    if (!form.name.trim()) {
      return setForm((f) => ({
        ...f,
        message: "Tên thể loại không được để trống!",
        success: false
      }));
    }

    setForm((f) => ({ ...f, submitting: true }));

    try {
      await axiosClient.post("/public/types", { name: form.name });
      setForm({
        name: "",
        message: "Thêm thể loại thành công!",
        success: true,
        submitting: false
      });
      onTypeAdded?.();
      if (shouldClose) {
        onSuccessClose?.();
      }
    } catch (err) {
      setForm((f) => ({
        ...f,
        message:
          "Thêm thất bại: " +
          (err.response?.data?.message || "Lỗi không xác định!"),
        success: false,
        submitting: false
      }));
    }
  };

  return (
    <div>
      {form.message && (
        <Alert
          type={form.success ? "success" : "error"}
          message={form.message}
          className="mb-3"
        />
      )}

      <Input
        placeholder="Nhập tên thể loại"
        size="large"
        value={form.name}
        onChange={(e) =>
          setForm((f) => ({ ...f, name: e.target.value }))
        }
        onPressEnter={() => handleAdd(false)} // Chỉ thêm, không đóng Modal
        className="mb-3"
      />

      <div className="d-flex justify-content-end gap-2">
        <Button
          onClick={() =>
            setForm((f) => ({ ...f, name: "", message: "", success: null }))
          }
        >
          Xoá
        </Button>
        <Button
          type="primary"
          loading={form.submitting}
          onClick={() => handleAdd(true)} // Thêm xong rồi đóng Modal
        >
          Thêm
        </Button>
      </div>
    </div>
  );
}
