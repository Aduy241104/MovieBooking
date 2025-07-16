import React, { useEffect, useState } from "react";
import { Modal, Button } from "antd";
import dayjs from "dayjs";
import "dayjs/locale/vi";
import { fetchAllActiveFareTypesAPI } from "../../../../service/TicketPriceService";

const InfoShowTimeModal = ({ visible, showtime, onClose }) => {
  const [fareType, setFareType] = useState(null);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    if (visible && showtime?.fareTypeId) {
      const fetchFareType = async () => {
        setLoading(true);
        try {
          const res = await fetchAllActiveFareTypesAPI();
          const responseData = res || {};
          if (responseData.result && responseData.result.length > 0) {
            const fareTypeData = responseData.result.find(
              (item) => item.id === showtime.fareTypeId
            );
            if (fareTypeData) {
              setFareType({
                name: fareTypeData.name,
                basePrice: fareTypeData.basePrice,
                dayPrice: fareTypeData.dayPrice,
                timeSlotType: fareTypeData.timeSlotType,
                movieFormat: fareTypeData.movieFormat,
              });
            } else {
              setFareType(null);
            }
          }
        } catch (err) {
          console.error("Error fetching fare type:", err);
          setFareType(null);
        } finally {
          setLoading(false);
        }
      };
      fetchFareType();
    } else {
      setFareType(null);
    }
  }, [visible, showtime?.fareTypeId]);

  return (
    <Modal
      title="THÔNG TIN LỊCH CHIẾU"
      open={visible}
      onCancel={onClose}
      okText="Đóng"
      cancelText="Hủy"
      maskClosable={false}
      footer={[
        <Button
          key="close"
          onClick={onClose}
          className="px-4 py-2 bg-gray-200 text-gray-800 rounded hover:bg-gray-300"
        >
          Đóng
        </Button>,
      ]}
      width={500}
    >
      <div className="bg-white p-4 rounded-lg shadow-md border border-gray-200">
        {loading ? (
          <div className="text-center text-gray-600">Đang tải...</div>
        ) : showtime ? (
          <div className="space-y-4">
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Tên phim</label>
              <p className="text-gray-600">{showtime.movieName || "Không có dữ liệu"}</p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Phòng chiếu</label>
              <p className="text-gray-600">{showtime.roomName || "Không có dữ liệu"}</p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Ngày chiếu</label>
              <p className="text-gray-600">
                {showtime.showDate
                  ? dayjs(showtime.showDate).format("DD/MM/YYYY")
                  : "Không có dữ liệu"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Giờ bắt đầu</label>
              <p className="text-gray-600">{showtime.startTime || "Không có dữ liệu"}</p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Giờ kết thúc</label>
              <p className="text-gray-600">{showtime.endTime || "Không có dữ liệu"}</p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Thời gian chiếu</label>
              <p className="text-gray-600">
                {showtime.showDateTime
                  ? dayjs(showtime.showDateTime).format("DD/MM/YYYY")
                  : "Không có dữ liệu"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Lồng tiếng / Phụ đề</label>
              <p className="text-gray-600">{fareType?.name || "Không có dữ liệu"}</p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Định dạng phim</label>
              <p className="text-gray-600">{fareType?.movieFormat || "Không có dữ liệu"}</p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Giá cơ bản</label>
              <p className="text-gray-600">
                {fareType?.basePrice
                  ? `${fareType.basePrice.toLocaleString("vi-VN")} VNĐ`
                  : "Không có dữ liệu"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Giá theo ngày</label>
              <p className="text-gray-600">
                {fareType?.dayPrice
                  ? `${fareType.dayPrice.toLocaleString("vi-VN")} VNĐ`
                  : "Không có dữ liệu"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Loại khung giờ</label>
              <p className="text-gray-600">{fareType?.timeSlotType || "Không có dữ liệu"}</p>
            </div>
            
          </div>
        ) : (
          <div className="text-center text-gray-600">Không có thông tin lịch chiếu</div>
        )}
      </div>
    </Modal>
  );
};

export default InfoShowTimeModal;