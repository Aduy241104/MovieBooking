import { DatePicker, Form, Modal, notification, Select } from "antd";
import { createShowtimeAPI } from "../../../../service/ShowtimeService";
import dayjs from "dayjs";
import { useEffect, useState } from "react";
import axiosInstance from "../../../../config/axios";
import { fetchAllActiveFareTypesAPI } from "../../../../service/TicketPriceService";

export const CreateShowtimeModal = (props) => {
  const { setRefreshFlag, isCreateModalOpen, setIsCreateModalOpen } = props;
  const [form] = Form.useForm();
  const [rooms, setRooms] = useState([]);
  const [movies, setMovies] = useState([]);
  const [fareTypes, setFareTypes] = useState([]);
  const token = localStorage.getItem("token");

  useEffect(() => {
    const fetchRooms = async () => {
      if (!token) {
        notification.error({
          message: "LỖI XÁC THỰC",
          description: "Không tìm thấy token. Vui lòng đăng nhập lại.",
        });
        return;
      }

      try {
        const res = await axiosInstance.get("/rooms", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });
        console.log("Rooms API response:", res);
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
        console.error("Fetch rooms error:", err.response || err);
        notification.error({
          message: "LỖI KẾT NỐI",
          description: `Không thể kết nối tới API phòng chiếu: ${err.message}`,
        });
      }
    };

    const fetchMovies = async () => {
      if (!token) {
        notification.error({
          message: "LỖI XÁC THỰC",
          description: "Không tìm thấy token. Vui lòng đăng nhập lại.",
        });
        return;
      }

      try {
        const res = await axiosInstance.get("/movies", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
          },
        });
        console.log("Movies API response:", res);
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
        console.error("Fetch movies error:", err.response || err);
        notification.error({
          message: "LỖI KẾT NỐI",
          description: `Không thể kết nối tới API phim: ${err.message}`,
        });
      }
    };

    const fetchFareTypes = async () => {
      if (!token) {
        notification.error({
          message: "LỖI XÁC THỰC",
          description: "Không tìm thấy token. Vui lòng đăng nhập lại.",
        });
        return;
      }

      try {
        const res = await fetchAllActiveFareTypesAPI();
        console.log("Fare types API response:", res);
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
        console.error("Fetch fare types error:", err.response || err);
        notification.error({
          message: "LỖI KẾT NỐI",
          description: `Không thể lấy dữ liệu loại vé: ${err.message}`,
        });
      }
    };

    if (isCreateModalOpen) {
      fetchRooms();
      fetchMovies();
      fetchFareTypes();
    }
  }, [isCreateModalOpen, token]);

  const handleSubmit = async (values) => {
    if (!token) {
      notification.error({
        message: "LỖI XÁC THỰC",
        description: "Không tìm thấy token. Vui lòng đăng nhập lại.",
      });
      return;
    }

    if (!values.movieId || !values.cinemaRoomId || !values.fareTypeId || !values.showDateTime) {
      notification.error({
        message: "DỮ LIỆU KHÔNG HỢP LỆ",
        description: "Vui lòng điền đầy đủ tất cả các trường bắt buộc.",
      });
      return;
    }

    const movieExists = movies.some((movie) => movie.id === values.movieId);
    const roomExists = rooms.some((room) => room.cinemaRoomId === values.cinemaRoomId);
    const fareTypeExists = fareTypes.some((fareType) => fareType.id === values.fareTypeId);

    if (!movieExists || !roomExists || !fareTypeExists) {
      notification.error({
        message: "DỮ LIỆU KHÔNG HỢP LỆ",
        description: `Vui lòng kiểm tra lại: ${
          !movieExists ? "Phim, " : ""
        }${!roomExists ? "Phòng chiếu, " : ""}${!fareTypeExists ? "Loại vé" : ""} không hợp lệ.`,
      });
      return;
    }

    try {
      const showtimeRequest = {
        movieId: Number(values.movieId),
        cinemaRoomId: Number(values.cinemaRoomId),
        fareTypeId: Number(values.fareTypeId),
        showDateTime: values.showDateTime.format("YYYY-MM-DD HH:mm:ss")
      };
      console.log("Showtime request (raw JSON):", JSON.stringify(showtimeRequest, null, 2));
      const res = await createShowtimeAPI(showtimeRequest);

      if (res && res.status === 201 && res.result) {
        notification.success({ message: "Thêm lịch chiếu thành công!" });
        setIsCreateModalOpen(false);
        form.resetFields();
        setRefreshFlag(true);
      } else {
        notification.error({
          message: "THẤT BẠI",
          description: res?.message || "Không xác định lỗi từ server",
        });
      }
    } catch (err) {
      console.error("Create showtime error:", err.response || err);
      const errorMessage = err.response?.data?.message || err.message || "Có lỗi xảy ra khi tạo lịch chiếu";
      notification.error({
        message: "THÊM THẤT BẠI",
        description: errorMessage,
      });
    }
  };

  return (
    <Modal
      title="THÊM LỊCH CHIẾU"
      open={isCreateModalOpen}
      onOk={() => form.submit()}
      onCancel={() => setIsCreateModalOpen(false)}
      afterClose={() => form.resetFields()}
      okText="Thêm mới"
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
            allowClear={false}
            styles={{ popup: { root: { maxHeight: 200, overflow: "auto" } } }}
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
            allowClear={false}
            styles={{ popup: { root: { maxHeight: 200, overflow: "auto" } } }}
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
            allowClear={false}
            styles={{ popup: { root: { maxHeight: 200, overflow: "auto" } } }}
          />
        </Form.Item>

        <Form.Item
          label="Ngày - giờ chiếu"
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