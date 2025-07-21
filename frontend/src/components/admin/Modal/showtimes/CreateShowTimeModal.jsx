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
      try {
        const res = await axiosInstance.get("/rooms", {
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
        const res = await axiosInstance.get("/movies", {
          headers: {
            Authorization: `Bearer ${token}`,
            "Content-Type": "application/json",
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

    if (isCreateModalOpen) {
      fetchRooms();
      fetchMovies();
      fetchFareTypes();
    }
  }, [isCreateModalOpen]);

  const handleSubmit = async (values) => {
    try {
      const res = await createShowtimeAPI({
        movieId: values.movieId,
        cinemaRoomId: values.cinemaRoomId,
        fareTypeId: values.fareTypeId,
        showDateTime: values.showDateTime.format("YYYY-MM-DDTHH:mm:ss"),
      });

      if (res.result) {
        notification.success({
          message: "THÊM THÀNH CÔNG",
          description: `Thêm lịch chiếu thành công`,
        });
        setRefreshFlag((prev) => !prev);
        setIsCreateModalOpen(false);
      } else {
        notification.error({
          message: "THÊM THẤT BẠI",
          description: `ERROR: ${res.message}`,
        });
      }
    } catch (err) {
      notification.error({
        message: "THÊM THẤT BẠI",
        description: "Có lỗi xảy ra khi tạo lịch chiếu",
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
              label: `${fareType.name} - ${fareType.movieFormat} - ${fareType.basePrice.toLocaleString('vi-VN')} VNĐ - ${fareType.dayPrice.toLocaleString('vi-VN')} VNĐ - ${fareType.timeSlotType || 'Không xác định'}`,
            }))}
            showSearch
            optionFilterProp="label"
            dropdownStyle={{ maxHeight: 200, overflow: "auto" }}
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
            disabledDate={(current) =>
              current && current < dayjs().startOf("day")
            }
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};
