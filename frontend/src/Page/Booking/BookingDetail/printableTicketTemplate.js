import { format, parseISO } from 'date-fns';
import { vi } from 'date-fns/locale';

/**
 * @param {object} bookingDetails -Object containing booking details.
 * @param {string} qrCodeHtml - The HTML string of the <svg> tag contains the QR code.
 * @returns {string} - Complete HTML String.
 */
export const createPrintableTicketHtml = (bookingDetails, qrCodeHtml) => {
    if (!bookingDetails) return "";

   // Reformat the data
    const formattedShowTime = format(parseISO(bookingDetails.screening.showDateTime), "HH:mm, EEEE, dd/MM/yyyy", { locale: vi });
    const formattedSeats = bookingDetails.bookedSeats.map(s => s.seatRow + s.seatCol).join(", ");
    const totalAmount = bookingDetails.totalAmount.toLocaleString("vi-VN");

    // Use Template Literals to create HTML strings
    return `
        <!DOCTYPE html>
        <html lang="vi">
            <head>
                <meta charset="UTF-8">
                <title>Vé xem phim - ${bookingDetails.bookingCode}</title>
                <style>
                    /* Import font từ Google Fonts */
                    @import url('https://fonts.googleapis.com/css2?family=Be+Vietnam+Pro:wght@400;500;700&display=swap');
                    
                    /* Reset và thiết lập trang in */
                    @page {
                        size: A5; /* Kích thước trang in, A5 là lựa chọn phổ biến cho vé */
                        margin: 0;
                    }
                    body {
                        font-family: 'Be Vietnam Pro', sans-serif;
                        font-size: 14px;
                        line-height: 1.6;
                        color: #333;
                        background-color: #f4f7f6;
                        margin: 0;
                        padding: 20px;
                        -webkit-print-color-adjust: exact !important; /* Đảm bảo màu nền được in trên Chrome */
                        color-adjust: exact !important; /* Chuẩn */
                    }
                    .ticket-container {
                        max-width: 450px;
                        margin: 0 auto;
                        background: #ffffff;
                        border-radius: 15px;
                        box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
                        overflow: hidden;
                    }
                    .header {
                        background: linear-gradient(90deg, #43cea2 0%, #185a9d 100%);
                        color: white;
                        padding: 20px;
                        text-align: center;
                    }
                    .header h2 {
                        margin: 0;
                        font-size: 24px;
                        font-weight: 700;
                    }
                    .header .booking-code {
                        font-size: 16px;
                        opacity: 0.9;
                        margin-top: 5px;
                    }
                    .body {
                        padding: 25px;
                    }
                    .movie-title {
                        font-size: 20px;
                        font-weight: 700;
                        color: #185a9d;
                        margin-bottom: 20px;
                        text-align: center;
                    }
                    .info-grid {
                        display: grid;
                        grid-template-columns: 1fr 2fr;
                        gap: 12px;
                        margin-bottom: 20px;
                    }
                    .info-grid strong {
                        font-weight: 500;
                        color: #555;
                    }
                    .info-grid span {
                        font-weight: 400;
                    }
                    .qr-section {
                        text-align: center;
                        border-top: 1px solid #eee;
                        padding-top: 20px;
                        margin-top: 20px;
                    }
                    .qr-section p {
                        font-size: 12px;
                        color: #777;
                        margin-top: 10px;
                    }
                    .footer {
                        text-align: center;
                        padding: 15px;
                        background-color: #f8f9fa;
                        font-size: 12px;
                        color: #888;
                    }
                </style>
            </head>
            <body>
                <div class="ticket-container">
                    <div class="header">
                        <h2>Vé Xem Phim Điện Tử</h2>
                        <div class="booking-code">Mã vé: <strong>${bookingDetails.bookingCode}</strong></div>
                    </div>
                    <div class="body">
                        <div class="movie-title">${bookingDetails.screening.movieNameVn}</div>
                        <div class="info-grid">
                            <strong>Suất chiếu:</strong>
                            <span>${formattedShowTime}</span>

                            <strong>Rạp / Phòng:</strong>
                            <span>${bookingDetails.screening.cinemaRoomName}</span>

                            <strong>Ghế:</strong>
                            <span>${formattedSeats}</span>

                            <strong>Khách hàng:</strong>
                            <span>${bookingDetails.account.fullName}</span>

                            <strong>Tổng tiền:</strong>
                            <span>${totalAmount}đ</span>
                        </div>
                        <div class="qr-section">
                            ${qrCodeHtml}
                            <p>Vui lòng đưa mã này cho nhân viên tại rạp</p>
                        </div>
                    </div>
                    <div class="footer">
                        Chúc quý khách xem phim vui vẻ!
                    </div>
                </div>
                <script>
                    setTimeout(function () { 
                        window.print(); 
                        window.close(); 
                    }, 500);
                </script>
            </body>
        </html>
    `;
};