// src/components/admin/Movie/MovieList.jsx
import React, { useState } from "react";
//import "./MovieList.css"; // nếu muốn style riêng

const MovieList = () => {
  const [showFullDescription, setShowFullDescription] = useState(false);

  const movie = {
    poster: "https://cinema.momocdn.net/img/80099757410724750-doemon.png?size=M",
    titleVN: "Doraemon Movie 44: Nobita và Cuộc Phiêu Lưu Vào Thế Giới Trong Tranh",
    titleEN: "Doraemon Movie 44: Nobita no Esekai Monogatari",
    duration: "105 phút",
    rating: 9.3,
    releaseDate: "23/05/2025",
    genres: ["Gia đình", "Phiêu lưu", "Giả tưởng", "Hoạt hình"],
    descriptionShort:
      "Doraemon Movie 44 cũng là tác phẩm kỷ niệm 45 năm ra mắt loạt phim \"Doraemon the Movie'\". Câu chuyện của phim kể về Doraemon, Nobita và những người...",
    descriptionFull:
      "Doraemon Movie 44 cũng là tác phẩm kỷ niệm 45 năm ra mắt loạt phim \"Doraemon the Movie'\". Câu chuyện của phim kể về Doraemon, Nobita và những người bạn bước vào một bức tranh đến thế giới châu Âu thời Trung cổ. Trong bức tranh, họ gặp những đứa trẻ đến từ đất nước Artoria. Họ cũng chạm trán một con quỷ nhỏ có cánh tên là Chai. Cùng nhau, họ đối mặt với một kẻ thù mạnh mẽ để giành lấy một viên ngọc huyền thoại.",
  };

  return (
    <div style={styles.container}>
      <h2>Danh sách phim</h2>
      <div style={styles.card}>
        <img src={movie.poster} alt="poster" style={styles.poster} />
        <div style={styles.info}>
          <h3 style={styles.title}>{movie.titleVN}</h3>
          <p style={styles.subTitle}>{movie.titleEN}</p>
          <p><strong>Thời lượng:</strong> {movie.duration}</p>
          <p><strong>Rating:</strong> ⭐ {movie.rating}</p>
          <p><strong>Ngày chiếu:</strong> {movie.releaseDate}</p>
          <p><strong>Thể loại:</strong> {movie.genres.join(", ")}</p>
          <p style={styles.desc}>
            {showFullDescription ? movie.descriptionFull : movie.descriptionShort}
            {!showFullDescription && (
              <button
                onClick={() => setShowFullDescription(true)}
                style={styles.moreBtn}
              >
                Xem thêm
              </button>
            )}
          </p>
        </div>
      </div>
    </div>
  );
};

const styles = {
  container: {
    padding: "20px",
    fontFamily: "sans-serif",
    maxWidth: "900px",
    margin: "0 auto",
  },
  card: {
    display: "flex",
    gap: "20px",
    border: "1px solid #ddd",
    padding: "20px",
    borderRadius: "10px",
    background: "#fff",
    boxShadow: "0 2px 5px rgba(0,0,0,0.1)",
  },
  poster: {
    width: "200px",
    height: "auto",
    borderRadius: "10px",
  },
  info: {
    flex: 1,
  },
  title: {
    margin: "0",
    fontSize: "20px",
    fontWeight: "bold",
  },
  subTitle: {
    margin: "4px 0 12px",
    fontSize: "14px",
    color: "#666",
  },
  desc: {
    marginTop: "10px",
    lineHeight: 1.6,
  },
  moreBtn: {
    marginLeft: "10px",
    background: "none",
    color: "blue",
    border: "none",
    cursor: "pointer",
    textDecoration: "underline",
    fontSize: "14px",
  },
};

export default MovieList;
