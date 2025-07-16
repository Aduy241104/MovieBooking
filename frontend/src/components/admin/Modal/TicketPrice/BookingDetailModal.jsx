import { Modal, Button } from "antd";
import dayjs from "dayjs";

const BookingDetailModal = ({ visible, booking, onClose }) => {
  return (
    <Modal
      title="HÓA ĐƠN ĐẶT VÉ"
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
        {booking ? (
          <div className="space-y-4">
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Tên phim</label>
              <p className="text-gray-600">
                {booking.screening?.movieNameVn ||
                  booking.screening?.movieNameEn ||
                  booking.screening?.movie?.nameVN ||
                  booking.screening?.movie?.nameEN ||
                  "Không xác định"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Khách hàng</label>
              <p className="text-gray-600">
                {booking.account?.fullName || booking.fullName || "Không xác định"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Tài khoản</label>
              <p className="text-gray-600">
                {booking.account?.email || booking.email || "Không xác định"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Phòng chiếu</label>
              <p className="text-gray-600">
                {booking.screening?.cinemaRoomName || "Không xác định"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Ghế</label>
              <p className="text-gray-600">
                {booking.bookedSeats?.length
                  ? booking.bookedSeats
                      .map((seat) => `${seat.seatCol}${seat.seatRow}`)
                      .join(", ")
                  : "Chưa chọn ghế"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Ngày giờ đặt</label>
              <p className="text-gray-600">
                {booking.bookingTime
                  ? dayjs(booking.bookingTime).format("DD/MM/YYYY | HH:mm")
                  : "Không có dữ liệu"}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Mã giảm giá</label>
              <p className="text-gray-600">
                {booking.promotionCodeApplied || "Không có mã giảm giá"}
                {booking.discountApplied
                  ? ` (-${booking.discountApplied.toLocaleString("vi-VN")} VNĐ)`
                  : ""}
              </p>
            </div>
            <div className="flex justify-between">
              <label className="font-medium text-gray-700">Phương thức thanh toán</label>
              <p className="text-gray-600">
                {booking.paymentMethod?.methodName ||
                  booking.paymentMethod ||
                  "Không có dữ liệu"}
              </p>
            </div>
            <hr className="border-t border-dashed border-gray-300 my-4" />
            <div className="flex justify-between">
              <label className="font-bold text-gray-700">Tổng tiền</label>
              <p className="text-red-600 font-bold">
                {booking.totalAmount
                  ? `${booking.totalAmount.toLocaleString("vi-VN")} VNĐ`
                  : "Không có dữ liệu"}
              </p>
            </div>
          </div>
        ) : (
          <div className="text-center text-gray-600">Không có thông tin booking</div>
        )}
      </div>
    </Modal>
  );
};

export default BookingDetailModal;
