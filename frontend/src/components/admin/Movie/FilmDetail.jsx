import React, { useEffect, useState } from "react";
import axios from "../../../config/axios";
import { Spin, message, Button, Modal } from "antd";
import { useParams } from "react-router-dom";
import "bootstrap/dist/css/bootstrap.min.css";

const FilmDetail = () => {
  const { id } = useParams(); // Lấy id từ URL
  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(false);
  const [trailerVisible, setTrailerVisible] = useState(false);

  const fetchMovieById = async (movieId) => {
    setLoading(true);
    try {
      console.log(`Sending request to /movies/getAll to find movie with id: ${movieId}`);
      const res = await axios.get("/movies/getAll");
      console.log("API response:", res);
      // Kiểm tra dữ liệu nằm ở res hay res.data
      const responseData = res.data || res;
      console.log("Response data:", responseData);
      console.log("Response content:", responseData.content);
      if (responseData && responseData.content && responseData.content.length > 0) {
        const foundMovie = responseData.content.find(
          (m) => m.id.toString() === movieId
        );
        console.log("Found movie:", foundMovie);
        if (foundMovie) {
          setMovie(foundMovie);
        } else {
          console.warn(`No movie found with id: ${movieId}`);
          message.warning("Không tìm thấy phim với id này");
        }
      } else {
        console.warn("No data in response:", responseData);
        message.warning("Không có dữ liệu phim");
      }
    } catch (err) {
      console.error("Error fetching movies:", {
        message: err.message,
        response: err.response,
        status: err.response?.status,
        data: err.response?.data,
      });
      message.error("Lỗi tải dữ liệu phim: " + (err.message || "Unknown error"));
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    if (id) {
      fetchMovieById(id);
    }
  }, [id]);

  if (loading || !movie) {
    return (
      <div className="d-flex justify-content-center align-items-center" style={{ minHeight: "400px" }}>
        <Spin size="large" />
      </div>
    );
  }

  return (
    <div className="d-flex justify-content-center" style={{ background: "#f0f2f5", padding: "20px" }}>
      <div style={{ background: "#fff", borderRadius: "8px", overflow: "hidden", width: "100%", maxWidth: "1200px" }}>
        {/* Banner */}
        <div
          style={{
            height: "300px",
            backgroundImage: `url(${movie.largeImage || 'https://via.placeholder.com/1200x300'})`,
            backgroundSize: "cover",
            backgroundPosition: "center",
          }}
        ></div>

        {/* Thông tin */}
        <div className="container position-relative" style={{ marginTop: "-50px", paddingLeft: "80px", paddingRight: "80px", paddingBottom: "20px" }}>
          <div className="row">
            <div className="col-md-3">
              <img
                src={movie.smallImage || 'https://via.placeholder.com/300x450'}
                alt="Poster"
                className="img-fluid rounded shadow"
              />
            </div>
            <div className="col-md-9">
              <div className="ps-md-4" style={{ marginTop: "60px", fontSize: "1.1rem" }}>
                <h2 style={{ fontSize: "2rem" }}>{movie.nameVN}</h2>
                <h5 className="text-muted" style={{ fontSize: "1.2rem" }}>{movie.nameEN}</h5>
                <p><strong>Giới hạn độ tuổi:</strong> {movie.ageLimit}+</p>
                <p><strong>Thể loại:</strong> {movie.genres || "Đang cập nhật"}</p>
                <p><strong>Rating:</strong> {movie.averageRating || "Chưa có"}</p>
                <p><strong>Thời lượng:</strong> {movie.duration} phút</p>
                <p><strong>Ngày khởi chiếu:</strong> {movie.fromDate}</p>
                <p><strong>Ngày kết thúc:</strong> {movie.toDate}</p>
                <p><strong>Đạo diễn:</strong> {movie.director}</p>
                <p><strong>Diễn viên:</strong> {movie.actor}</p>
                <p><strong>Nhà sản xuất:</strong> {movie.movieProductionCompany}</p>

                {movie.trailer && (
                  <Button
                    type="primary"
                    onClick={() => setTrailerVisible(true)}
                    style={{ marginTop: "10px" }}
                  >
                    Xem Trailer
                  </Button>
                )}
              </div>
            </div>
          </div>

          <div className="mt-4" style={{ fontSize: "1.1rem" }}>
            <div style={{ display: "flex", alignItems: "center", marginBottom: "10px" }}>
              <div style={{ width: "4px", height: "24px", backgroundColor: "#007bff", marginRight: "10px" }}></div>
              <h4 style={{ margin: 0, fontSize: "1.4rem" }}>Nội Dung Phim</h4>
            </div>
            <p>{movie.content}</p>
          </div>
        </div>

        <Modal
          title="Trailer phim"
          open={trailerVisible}
          footer={null}
          onCancel={() => setTrailerVisible(false)}
          width={800}
        >
          <div style={{ position: "relative", paddingBottom: "56.25%", height: 0 }}>
            <iframe
              src={movie.trailer.replace("watch?v=", "embed/")}
              title="Trailer"
              frameBorder="0"
              allowFullScreen
              style={{
                position: "absolute",
                top: 0,
                left: 0,
                width: "100%",
                height: "100%",
              }}
            ></iframe>
          </div>
        </Modal>
      </div>
    </div>
  );
};

export default FilmDetail;