import { DatePicker, Form, Input, Modal, notification, Select } from "antd";
import { createShowtimeAPI } from "../../../../service/ShowtimeService";
import { useOutletContext } from "react-router-dom";
import dayjs from "dayjs";
import { useEffect, useState } from "react";
import axiosInstance from "../../../../config/axios";
import { fetchAllActiveFareTypesAPI } from "../../../../service/TicketPriceService";

export const CreateShowtimeModal = (props) => {
  const { setRefreshFlag } = props;
  const { isCreateModalOpen, setIsCreateModalOpen } = props;
  const [form] = Form.useForm();
  const [rooms, setRooms] = useState([]);
  const [movies, setMovies] = useState([]);
  const [fareTypes, setFareTypes] = useState([]);

  useEffect(() => {
    const fetchRooms = async () => {
      try {
        console.log("Sending request to /rooms");
        const res = await axiosInstance.get("/rooms");
        console.log("Rooms API response:", res);
        const responseData = res.data || res;
        console.log("Rooms response data:", responseData);
        if (Array.isArray(responseData)) {
          const activeRooms = responseData.filter((room) => !room.isDeleted);
          console.log("Active rooms:", activeRooms);
          setRooms(activeRooms);
        } else {
          console.warn("Invalid rooms data:", responseData);
          notification.error({
            message: "LỖI DỮ LIỆU",
            description: "API trả về dữ liệu phòng chiếu không hợp lệ",
          });
        }
      } catch (err) {
        console.error("Error fetching rooms:", {
          message: err.message,
          response: err.response,
          status: err.response?.status,
          data: err.response?.data,
        });
        notification.error({
          message: "LỖI KẾT NỐI",
          description: "Không thể kết nối tới API phòng chiếu",
        });
      }
    };

    const fetchMovies = async () => {
      try {
        console.log("Sending request to /movies/getAll");
        const res = await axiosInstance.get("/movies/getAll");
        console.log("Movies API response:", res);
        const responseData = res.data || res;
        console.log("Movies response data:", responseData);
        console.log("Movies response content:", responseData.content);
        if (responseData && responseData.content && Array.isArray(responseData.content)) {
          console.log("Movies:", responseData.content);
          setMovies(responseData.content);
        } else {
          console.warn("Invalid movies data:", responseData);
          notification.error({
            message: "LỖI DỮ LIỆU",
            description: "API trả về dữ liệu phim không hợp lệ",
          });
        }
      } catch (err) {
        console.error("Error fetching movies:", {
          message: err.message,
          response: err.response,
          status: err.response?.status,
          data: err.response?.data,
        });
        notification.error({
          message: "LỖI KẾT NỐI",
          description: "Không thể kết nối tới API phim",
        });
      }
    };

    const fetchFareTypes = async () => {
      try {
        console.log("Bắt đầu gọi API fetchAllActiveFareTypesAPI: /fare-types/getAll");
        const startTime = performance.now();
        const res = await fetchAllActiveFareTypesAPI();
        const endTime = performance.now();
        console.log(`API fetchAllActiveFareTypesAPI hoàn thành trong ${(endTime - startTime).toFixed(2)}ms`);

        console.log("FareTypes API raw response:", res);
        if (!res) {
          throw new Error("Phản hồi API là undefined hoặc null");
        }

        const responseData = res.data || res;
        console.log("FareTypes response data:", responseData);
        console.log("FareTypes response status:", res.status || "N/A");
        console.log("FareTypes response headers:", res.headers || "N/A");

        const fareTypeData = responseData.result || responseData;
        if (Array.isArray(fareTypeData)) {
          console.log("Danh sách loại vé hợp lệ:", fareTypeData);
          setFareTypes(fareTypeData);
        } else {
          console.warn("Dữ liệu loại vé không phải là mảng:", fareTypeData);
          notification.error({
            message: "LỖI DỮ LIỆU",
            description: "API trả về dữ liệu loại vé không hợp lệ",
          });
        }
      } catch (err) {
        console.error("Lỗi khi gọi API fetchAllActiveFareTypesAPI:", {
          message: err.message,
          response: err.response,
          status: err.response?.status,
          data: err.response?.data,
          stack: err.stack,
        });
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
      console.log("Submitting showtime with values:", values);
      const res = await createShowtimeAPI({
        movieId: values.movieId,
        cinemaRoomId: values.cinemaRoomId,
        fareTypeId: values.fareTypeId,
        showDateTime: values.showDateTime.format("YYYY-MM-DDTHH:mm:ss"),
      });
      console.log("Create showtime response:", res);

      if (res.result) {
        notification.success({
          message: "THÊM THÀNH CÔNG",
          description: `Thêm lịch chiếu thành công`,
        });
        setRefreshFlag((prev) => !prev);
        setIsCreateModalOpen(false);
      } else {
        console.warn("Create showtime failed:", res.message);
        notification.error({
          message: "THÊM THẤT BẠI",
          description: `ERROR: ${res.message}`,
        });
      }
    } catch (err) {
      console.error("Error creating showtime:", {
        message: err.message,
        response: err.response,
        status: err.response?.status,
        data: err.response?.data,
      });
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
            disabledDate={(current) => current && current < dayjs().startOf("day")}
          />
        </Form.Item>
      </Form>
    </Modal>
  );
};