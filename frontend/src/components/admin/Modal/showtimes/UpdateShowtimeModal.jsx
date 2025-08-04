import { DatePicker, Form, Modal, notification, Select } from "antd";
import dayjs from "dayjs";
import { useEffect, useState } from "react";
import axiosInstance from "../../../../config/axios";
import axios from "axios";
import { fetchAllActiveFareTypesAPI } from "../../../../service/TicketPriceService";

export const UpdateShowtimeModal = (props) => {
  const {
    setRefreshFlag,
    isUpdateModalOpen,
    setIsUpdateModalOpen,
    showtimeData,
  } = props;

  const [form] = Form.useForm();
  const [rooms, setRooms] = useState([]);
  const [movies, setMovies] = useState([]);
  const [fareTypes, setFareTypes] = useState([]);
  const token = localStorage.getItem("token");

  useEffect(() => {
    const fetchRooms = async () => {
      try {
        const res = await axios.get("http://localhost:8081/api/rooms", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });

        const responseData = res.data || res;
        if (Array.isArray(responseData)) {
          const activeRooms = responseData.filter((room) => !room.isDeleted);
          setRooms(activeRooms);
        } else {
          notification.error({
            message: "LỖI DỮ LIỆU",
            description: "API trả về dữ liệu phòng chiếu không hợp lệ",
          });
        }
      } catch (err) {
        notification.error({
          message: "LỖI KẾT NỐI",
          description: "Không thể kết nối tới API phòng chiếu",
        });
      }
    };

    const fetchMovies = async () => {
      try {
        const res = await axios.get("http://localhost:8081/api/movies", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "multipart/form-data",
          },
        });

        const responseData = res.data || res;
        if (Array.isArray(responseData)) {
          setMovies(responseData);
        } else if (responseData && Array.isArray(responseData.content)) {
          setMovies(responseData.content);
        } else {
          notification.error({
            message: "LỖI DỮ LIỆU",
            description: "API trả về dữ liệu phim không hợp lệ",
          });
        }
      } catch (err) {
        notification.error({
          message: "LỖI KẾT NỐI",
          description: "Không thể kết nối tới API phim",
        });
      }
    };

    const fetchFareTypes = async () => {
      try {
        const res = await fetchAllActiveFareTypesAPI();
        const responseData = res.data || res;
        const fareTypeData = responseData.result || responseData;
        if (Array.isArray(fareTypeData)) {
          setFareTypes(fareTypeData);
        } else {
          notification.error({
            message: "LỖI DỮ LIỆU",
            description: "API trả về dữ liệu loại vé không hợp lệ",
          });
        }
      } catch (err) {
        notification.error({
          message: "LỖI KẾT NỐI",
          description: `Không thể lấy dữ liệu loại vé: ${err.message}`,
        });
      }
    };

    if (isUpdateModalOpen) {
      fetchRooms();
      fetchMovies();
      fetchFareTypes();

      if (showtimeData) {
        form.setFieldsValue({
          movieId: showtimeData.movieId,
          cinemaRoomId: showtimeData.cinemaRoomId,
          fareTypeId: showtimeData.fareTypeId,
          showDateTime: dayjs(showtimeData.showDateTime),
        });
      }
    }
  }, [isUpdateModalOpen, showtimeData, form, token]);

  const handleSubmit = async (values) => {
    try {
      if (!showtimeData?.id) {
        notification.error({
          message: "Thiếu ID lịch chiếu",
          description: "Không tìm thấy ID lịch chiếu để cập nhật!",
        });
        return;
      }

      const body = {
        movieId: Number(values.movieId),
        cinemaRoomId: Number(values.cinemaRoomId),
        fareTypeId: Number(values.fareTypeId),
        showDateTime: values.showDateTime.format("YYYY-MM-DDTHH:mm:ss"),
      };

      const res = await axiosInstance.put(
        `/movieSchedule/admin/update-time/${showtimeData.id}`,
        body,
        {
          headers: {
            Authorization: `Bearer ${token}`,
          },
        }
      );

      const data = res.data || res;

      if (data.status === 200) {
        notification.success({
          message: "CẬP NHẬT THÀNH CÔNG",
          description: "Lịch chiếu đã được cập nhật!",
        });
        setRefreshFlag((prev) => !prev);
        setIsUpdateModalOpen(false);
      } else {
        notification.error({
          message: "CẬP NHẬT THẤT BẠI",
          description: data.message || "Có lỗi xảy ra",
        });
      }
    } catch (err) {
      notification.error({
        message: "CẬP NHẬT THẤT BẠI",
        description: err?.response?.data?.message || "Không thể kết nối server!",
      });
    }
  };

  return (
    <Modal
      title="CẬP NHẬT LỊCH CHIẾU"
      open={isUpdateModalOpen}
      onOk={() => form.submit()}
      onCancel={() => setIsUpdateModalOpen(false)}
      afterClose={() => form.resetFields()}
      okText="Cập nhật"
      cancelText="Huỷ"
      maskClosable={false}
    >
      <Form form={form} layout="vertical" onFinish={handleSubmit}>
        <Form.Item
          label="Chọn phim"
          name="movieId"
          rules={[{ required: true, message: "Vui lòng chọn phim!" }]}
        >
          <Select
            placeholder="Chọn phim"
            options={movies.map((movie) => ({
              value: movie.id,
              label: movie.nameVN,
            }))}
            showSearch
            optionFilterProp="label"
            dropdownStyle={{ maxHeight: 200, overflow: "auto" }}
          />
        </Form.Item>

        <Form.Item
          label="Chọn phòng chiếu"
          name="cinemaRoomId"
          rules={[{ required: true, message: "Vui lòng chọn phòng chiếu!" }]}
        >
          <Select
            placeholder="Chọn phòng chiếu"
            options={rooms.map((room) => ({
              value: room.cinemaRoomId,
              label: room.cinemaRoomName,
            }))}
            showSearch
            optionFilterProp="label"
            dropdownStyle={{ maxHeight: 200, overflow: "auto" }}
          />
        </Form.Item>

        <Form.Item
          label="Chọn loại vé"
          name="fareTypeId"
          rules={[{ required: true, message: "Vui lòng chọn loại vé!" }]}
        >
          <Select
            placeholder="Chọn loại vé"
            options={fareTypes.map((fareType) => ({
              value: fareType.id,
              label: `${fareType.name} - ${fareType.movieFormat} - ${fareType.basePrice.toLocaleString(
                "vi-VN"
              )} VNĐ - ${fareType.dayPrice.toLocaleString("vi-VN")} VNĐ - ${
                fareType.timeSlotType || "Không xác định"
              }`,
            }))}
            showSearch
            optionFilterProp="label"
            dropdownStyle={{ maxHeight: 200, overflow: "auto" }}
          />
        </Form.Item>

        <Form.Item
          label="Ngày giờ chiếu"
          name="showDateTime"
          rules={[{ required: true, message: "Vui lòng chọn ngày giờ chiếu!" }]}
        >
          <DatePicker
            showTime
            format="YYYY-MM-DD HH:mm"
            style={{ width: "100%" }}
            disabledDate={(current) => current && current < dayjs().startOf("day")}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
