import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { Button, Table, Container, Row, Col } from 'react-bootstrap';
import { useNavigate, useLocation, useOutletContext } from 'react-router-dom';

export default function MovieList() {
  const [movies, setMovies] = useState([]);
  const navigate = useNavigate();
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();

  useEffect(() => {
    if (location.pathname.includes('/admin/movie')) {
      setBreadcrumbItems([
        { title: 'Trang chủ' },
        { title: 'Quản lý phim' },
        { title: 'Phim' },

      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);

  const fetch = () => {
    axios.get('http://localhost:8081/api/public/movies')
      .then(r => setMovies(r.data))
      .catch(err => console.error('Lỗi khi lấy danh sách phim:', err));
  };

  useEffect(() => {
    fetch();
  }, []);

  const handleEdit = (id) => {
    navigate(`/admin/movies/edit/${id}`);
  };

  const handleAdd = () => {
    navigate('/admin/movies/add');
  };

  const handleDelete = (id) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa phim này không?')) {
      axios.delete(`http://localhost:8081/api/public/movies/${id}`)
        .then(() => {
          fetch();
        })
        .catch(err => {
          console.error('Lỗi khi xóa phim:', err);
          alert('Xóa phim thất bại.');
        });
    }
  };

  return (
    <Container className="py-4">
      <Row>
        <Col>
          <h2 className="mb-4">Danh sách phim</h2>
          <Button variant="success" onClick={handleAdd} className="mb-3">
            Thêm phim mới
          </Button>
        </Col>
      </Row>

      <Row>
        <Col>
          <Table bordered hover responsive>
            <thead className="table-dark">
              <tr>
                <th>#</th>
                <th>Tên phim (VN)</th>
                <th>Thể loại</th>
                <th>Ngày chiếu</th>
                <th>Ngày kết thúc</th>
                <th>Thời lượng</th>
                <th>Tuổi</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {movies.map((m, i) => (
                <tr key={m.id}>
                  <td>{i + 1}</td>
                  <td>{m.nameVN}</td>
                  <td>{m.typeNames?.join(', ')}</td>
                  <td>{m.fromDate}</td>
                  <td>{m.toDate}</td>
                  <td>{m.duration} phút</td>
                  <td>{m.ageLimit}+</td>
                  <td>
                    <Button variant="primary" size="sm" onClick={() => handleEdit(m.id)} className="me-2">
                      Sửa
                    </Button>
                    <Button variant="danger" size="sm" onClick={() => handleDelete(m.id)}>
                      Xóa
                    </Button>
                  </td>
                </tr>
              ))}
              {movies.length === 0 && (
                <tr>
                  <td colSpan="8" className="text-center text-muted">
                    Không có phim nào.
                  </td>
                </tr>
              )}
            </tbody>
          </Table>
        </Col>
      </Row>
    </Container>
  );
}
