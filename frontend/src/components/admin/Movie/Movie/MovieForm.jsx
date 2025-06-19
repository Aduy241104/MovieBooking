import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Form, Button, Row, Col, Dropdown, Badge } from 'react-bootstrap';

export default function MovieForm({ movieId, onSuccess }) {
  const isEdit = !!movieId;

  const [form, setForm] = useState({
    nameVN: '', nameEN: '', duration: '', content: '', fromDate: '',
    toDate: '', director: '', actor: '', movieProductionCompany: '',
    ageLimit: '', trailerLink: '', typeIds: [],
    smallImageUrl: '', largeImageUrl: ''
  });

  const [smallImage, setSmallImage] = useState(null);
  const [largeImage, setLargeImage] = useState(null);
  const [previewSmallImage, setPreviewSmallImage] = useState(null);
  const [previewLargeImage, setPreviewLargeImage] = useState(null);
  const [types, setTypes] = useState([]);

  useEffect(() => {
    axios.get('http://localhost:8081/api/public/types')
      .then(r => setTypes(r.data));

    if (isEdit) {
      axios.get(`http://localhost:8081/api/public/movies/${movieId}`).then(r => {
        const m = r.data;
        setForm(prev => ({
          ...prev,
          nameVN: m.nameVN,
          nameEN: m.nameEN,
          duration: m.duration,
          content: m.content,
          fromDate: m.fromDate,
          toDate: m.toDate,
          director: m.director,
          actor: m.actor,
          movieProductionCompany: m.movieProductionCompany,
          ageLimit: m.ageLimit,
          trailerLink: m.trailerUrl || '',
          smallImageUrl: m.smallImageUrl,
          largeImageUrl: m.largeImageUrl,
          typeIds: m.typeIds || []
        }));
      });
    }
  }, [movieId, isEdit]);

  useEffect(() => {
    return () => {
      if (previewSmallImage) URL.revokeObjectURL(previewSmallImage);
      if (previewLargeImage) URL.revokeObjectURL(previewLargeImage);
    };
  }, [previewSmallImage, previewLargeImage]);

  const handleChange = e => {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
  };

  const handleSubmit = e => {
    e.preventDefault();
    const data = new FormData();
    Object.keys(form).forEach(key => {
      if (key === 'typeIds') {
        form.typeIds.forEach(id => data.append('typeIds', id));
      } else if (form[key] !== '' && form[key] !== null) {
        data.append(key, form[key]);
      }
    });
    if (smallImage) data.append('smallImage', smallImage);
    if (largeImage) data.append('largeImage', largeImage);

    const url = isEdit
      ? `http://localhost:8081/api/public/movies/${movieId}`
      : 'http://localhost:8081/api/public/movies';

    axios.post(url, data, {
      headers: { 'Content-Type': 'multipart/form-data' }
    })
    .then(r => onSuccess(r.data))
    .catch(err => {
      console.error('Lỗi khi gửi phim:', err);
      alert('Có lỗi xảy ra khi gửi dữ liệu phim. Xem console để biết thêm.');
    });
  };

  const handleSelectType = id => {
    setForm(prev => {
      const newTypeIds = prev.typeIds.includes(id)
        ? prev.typeIds.filter(typeId => typeId !== id)
        : [...prev.typeIds, id];
      return { ...prev, typeIds: newTypeIds };
    });
  };

  const handleRemoveType = id => {
    setForm(prev => ({
      ...prev,
      typeIds: prev.typeIds.filter(typeId => typeId !== id)
    }));
  };

  return (
    <Form onSubmit={handleSubmit} className="p-4 border rounded bg-light shadow-sm" encType="multipart/form-data">
      <h4>{isEdit ? 'Cập nhật phim' : 'Thêm phim mới'}</h4>
      <Row>
        <Col md={6}>
          <Form.Group className="mb-3">
            <Form.Label>Tên phim (VN)</Form.Label>
            <Form.Control name="nameVN" value={form.nameVN} onChange={handleChange} required />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Tên phim (EN)</Form.Label>
            <Form.Control name="nameEN" value={form.nameEN} onChange={handleChange} required />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Thời lượng (phút)</Form.Label>
            <Form.Control name="duration" type="number" value={form.duration} onChange={handleChange} required />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Giới hạn tuổi</Form.Label>
            <Form.Control name="ageLimit" type="number" value={form.ageLimit} onChange={handleChange} />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Ngày chiếu</Form.Label>
            <Form.Control name="fromDate" type="date" value={form.fromDate} onChange={handleChange} />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Ngày kết thúc</Form.Label>
            <Form.Control name="toDate" type="date" value={form.toDate} onChange={handleChange} />
          </Form.Group>
        </Col>
        <Col md={6}>
          <Form.Group className="mb-3">
            <Form.Label>Đạo diễn</Form.Label>
            <Form.Control name="director" value={form.director} onChange={handleChange} />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Diễn viên</Form.Label>
            <Form.Control name="actor" value={form.actor} onChange={handleChange} />
          </Form.Group>
          <Form.Group className="mb-3">
            <Form.Label>Hãng sản xuất</Form.Label>
            <Form.Control name="movieProductionCompany" value={form.movieProductionCompany} onChange={handleChange} />
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Ảnh nhỏ</Form.Label>
            <Form.Control type="file" accept="image/*" onChange={e => {
              const file = e.target.files[0];
              setSmallImage(file);
              if (file) setPreviewSmallImage(URL.createObjectURL(file));
            }} />
            {previewSmallImage ? (
              <img src={previewSmallImage} alt="Preview Small" style={{ maxWidth: 100, marginTop: 8 }} />
            ) : form.smallImageUrl && (
              <img src={`http://localhost:8081${form.smallImageUrl}`} alt="Small" style={{ maxWidth: 100, marginTop: 8 }} />
            )}
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Ảnh lớn</Form.Label>
            <Form.Control type="file" accept="image/*" onChange={e => {
              const file = e.target.files[0];
              setLargeImage(file);
              if (file) setPreviewLargeImage(URL.createObjectURL(file));
            }} />
            {previewLargeImage ? (
              <img src={previewLargeImage} alt="Preview Large" style={{ maxWidth: 100, marginTop: 8 }} />
            ) : form.largeImageUrl && (
              <img src={`http://localhost:8081${form.largeImageUrl}`} alt="Large" style={{ maxWidth: 100, marginTop: 8 }} />
            )}
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Trailer (YouTube link)</Form.Label>
            <Form.Control
              name="trailerLink"
              value={form.trailerLink}
              onChange={handleChange}
              placeholder="Nhập link YouTube trailer"
            />
          </Form.Group>
        </Col>
      </Row>

      <Form.Group className="mb-3">
        <Form.Label>Nội dung phim</Form.Label>
        <Form.Control as="textarea" name="content" rows={4} value={form.content} onChange={handleChange} />
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>Thể loại</Form.Label>
        <Dropdown>
          <Dropdown.Toggle variant="light" className="w-100 text-start">
            {form.typeIds.length > 0 ? (
              form.typeIds.map(id => {
                const type = types.find(t => t.id === id);
                return (
                  <Badge key={id} bg="primary" className="me-1">
                    {type?.name || 'Unknown'}{' '}
                    <span style={{ cursor: 'pointer' }} onClick={e => {
                      e.stopPropagation();
                      handleRemoveType(id);
                    }}>×</span>
                  </Badge>
                );
              })
            ) : (
              <span className="text-muted">Chọn thể loại</span>
            )}
          </Dropdown.Toggle>
          <Dropdown.Menu>
            {types.map(t => (
              <Dropdown.Item
                key={t.id}
                active={form.typeIds.includes(t.id)}
                onClick={() => handleSelectType(t.id)}
              >
                {t.name}
              </Dropdown.Item>
            ))}
          </Dropdown.Menu>
        </Dropdown>
      </Form.Group>

      <Button type="submit" variant="primary">{isEdit ? 'Cập nhật' : 'Thêm phim'}</Button>
    </Form>
  );
}
