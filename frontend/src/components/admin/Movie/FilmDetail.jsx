import React, { useEffect, useState } from "react";
import axios from "../../../config/axios";
import { Spin, message, Button, Modal } from "antd";
import { useParams } from "react-router-dom";
import { PlayCircleOutlined, StarFilled } from "@ant-design/icons";
import "bootstrap/dist/css/bootstrap.min.css";
import { fetchArtistAPI, findMovieByNameAPI } from "../../../service/TmdbService";
import { getAverageRatingAndCountByMovieId } from "../../../service/ReviewService";

const FilmDetail = () => {
  const { id } = useParams();
  const [movie, setMovie] = useState(null);
  const [loading, setLoading] = useState(false);
  const [trailerVisible, setTrailerVisible] = useState(false);
  const [hoverBanner, setHoverBanner] = useState(false);
  const [hoverPoster, setHoverPoster] = useState(false);
  const [artistList, setArtistList] = useState([]);
  const [averageRating, setAverageRating] = useState(null);
  const [totalReviews, setTotalReviews] = useState(0);

  const fetchMovieById = async (movieId) => {
    setLoading(true);
    try {
      const res = await axios.get("http://localhost:8081/api/public/movies");
      const movies = res.data || res;
      const foundMovie = movies.find((m) => m.id.toString() === movieId);
      if (foundMovie) {
        setMovie(foundMovie);
        fetchArtist(foundMovie.nameEN);
        const { averageRating, totalApproved } = await getAverageRatingAndCountByMovieId(foundMovie.id);
        setAverageRating(averageRating);
        setTotalReviews(totalApproved);
      } else message.warning("Không tìm thấy phim với id này");
    } catch (err) {
      message.error("Lỗi tải dữ liệu phim: " + (err.message || "Unknown error"));
    } finally {
      setLoading(false);
    }
  };

  const fetchArtist = async (movieName) => {
    try {
      const response = await findMovieByNameAPI(movieName);
      const artistList = await fetchArtistAPI(response.results[0].id);
      setArtistList(artistList.cast.slice(0, 12));
    } catch (error) {
      console.log(error);
    }
  };

  useEffect(() => {
    if (id) fetchMovieById(id);
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
      <div style={{ background: "#fff", borderRadius: "8px", overflow: "hidden", width: "100%", maxWidth: "1000px" }}>
        {/* Banner */}
        <div
          onMouseEnter={() => setHoverBanner(true)}
          onMouseLeave={() => setHoverBanner(false)}
          onClick={() => movie.trailerUrl && setTrailerVisible(true)}
          style={{
            position: "relative",
            height: "280px",
            backgroundImage: `url(${movie.largeImageUrl || 'https://via.placeholder.com/1200x300'})`,
            backgroundSize: "cover",
            backgroundPosition: "center",
            cursor: movie.trailerUrl ? "pointer" : "default",
          }}
        >
          {hoverBanner && movie.trailerUrl && (
            <div
              style={{
                position: "absolute",
                top: 0,
                left: 0,
                right: 0,
                bottom: 0,
                backgroundColor: "rgba(0,0,0,0.4)",
                display: "flex",
                justifyContent: "center",
                alignItems: "center",
              }}
            >
              <PlayCircleOutlined style={{ fontSize: 64, color: "#fff" }} />
            </div>
          )}
        </div>

        {/* Info section */}
        <div className="container position-relative" style={{ marginTop: "-50px", padding: "20px 60px" }}>
          <div className="row">
            <div className="col-md-3 position-relative">
              <div
                onMouseEnter={() => setHoverPoster(true)}
                onMouseLeave={() => setHoverPoster(false)}
                onClick={() => movie.trailerUrl && setTrailerVisible(true)}
                style={{ position: "relative", cursor: movie.trailerUrl ? "pointer" : "default" }}
              >
                <img
                  src={movie.smallImageUrl || 'https://via.placeholder.com/300x450'}
                  alt="Poster"
                  className="img-fluid rounded shadow"
                />
                {hoverPoster && movie.trailerUrl && (
                  <div
                    style={{
                      position: "absolute",
                      top: 0,
                      left: 0,
                      right: 0,
                      bottom: 0,
                      backgroundColor: "rgba(0,0,0,0.4)",
                      display: "flex",
                      justifyContent: "center",
                      alignItems: "center",
                      borderRadius: "5px",
                    }}
                  >
                    <PlayCircleOutlined style={{ fontSize: 48, color: "#fff" }} />
                  </div>
                )}
              </div>
            </div>

            <div className="col-md-9">
              <div className="ps-md-4" style={{ marginTop: "50px", fontSize: "1rem" }}>
                <h2>{movie.nameVN}</h2>
                <h5 className="text-muted">{movie.nameEN}</h5>
                <p><strong>Giới hạn độ tuổi:</strong> {movie.ageLimit}+</p>
                <p><strong>Thể loại:</strong> {movie.typeNames?.join(", ") || "Đang cập nhật"}</p>

                <p className="d-flex align-items-center gap-2">
                  <strong>Rating:</strong>
                  {averageRating !== null ? (
                    <span style={{ display: "flex", alignItems: "center" }}>
                      <StarFilled style={{ fontSize: 20, color: "#fadb14", marginRight: 6 }} />
                      <span style={{ fontSize: "1.5rem", fontWeight: "bold", marginRight: 4 }}>
                        {averageRating.toFixed(1)}
                      </span>
                      <span style={{ fontSize: "0.9rem", color: "#888" }}>
                        /10 · {totalReviews} đánh giá
                      </span>
                    </span>
                  ) : (
                    <span style={{ fontSize: "0.95rem", color: "#888" }}>Chưa có đánh giá</span>
                  )}
                </p>

                <p><strong>Thời lượng:</strong> {movie.duration} phút</p>
                <p><strong>Ngày khởi chiếu:</strong> {movie.fromDate}</p>
                <p><strong>Ngày kết thúc:</strong> {movie.toDate}</p>
                <p><strong>Đạo diễn:</strong> {movie.director}</p>
                <p><strong>Nhà sản xuất:</strong> {movie.movieProductionCompany}</p>
                <p><strong>Diễn viên:</strong></p>

                {/* Diễn viên */}
                <div className="d-flex flex-wrap mt-3">
                  {artistList.map((item, index) => (
                    <div className="me-3 mb-3 text-center" key={index}>
                      <div
                        style={{
                          width: "60px",
                          height: "60px",
                          borderRadius: "999px",
                          overflow: "hidden",
                          margin: "0 auto",
                        }}
                      >
                        <img
                          style={{ width: "100%", height: "100%", objectFit: "cover" }}
                          src={`https://image.tmdb.org/t/p/w200/${item.profile_path}`}
                          alt=""
                        />
                      </div>
                      <p
                        className="fs-8 fw-medium text-center mt-1"
                        style={{ fontSize: "0.75rem", maxWidth: "64px" }}
                      >
                        {item.character}
                      </p>
                    </div>
                  ))}
                </div>

              </div>
            </div>
          </div>

          <div className="mt-4" style={{ fontSize: "1rem" }}>
            <div style={{ display: "flex", alignItems: "center", marginBottom: "10px" }}>
              <div style={{ width: "4px", height: "24px", backgroundColor: "#007bff", marginRight: "10px" }}></div>
              <h4 style={{ margin: 0 }}>Nội Dung Phim</h4>
            </div>
            <p>{movie.content}</p>
          </div>
        </div>

        {/* Modal trailer */}
        <Modal
          title="Trailer phim"
          open={trailerVisible}
          footer={null}
          onCancel={() => setTrailerVisible(false)}
          width={800}
        >
          <div style={{ position: "relative", paddingBottom: "56.25%", height: 0 }}>
            {movie.trailerUrl.endsWith(".mp4") ? (
              <video controls style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%" }}>
                <source src={movie.trailerUrl} type="video/mp4" />
                Trình duyệt không hỗ trợ video.
              </video>
            ) : (
              <iframe
                src={
                  movie.trailerUrl.includes("watch?v=")
                    ? movie.trailerUrl.replace("watch?v=", "embed/")
                    : movie.trailerUrl
                }
                title="Trailer"
                frameBorder="0"
                allowFullScreen
                style={{ position: "absolute", top: 0, left: 0, width: "100%", height: "100%" }}
              ></iframe>
            )}
          </div>
        </Modal>
      </div>
    </div>
  );
};

export default FilmDetail;
