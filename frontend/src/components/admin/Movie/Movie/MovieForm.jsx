import React, { useState, useEffect } from 'react';
import axios from 'axios';
import { Form, Button, Row, Col, Dropdown, Badge } from 'react-bootstrap';
import { BadgePlus, Pencil, X } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { message } from 'antd';

export default function MovieForm({ movieId, onSuccess }) {
  const isEdit = !!movieId;

  const [form, setForm] = useState({
    nameVN: '', nameEN: '', duration: '', content: '', fromDate: '',
    toDate: '', director: '', actor: '', movieProductionCompany: '',
    ageLimit: '', trailerLink: '', typeIds: [],
    smallImageUrl: '', largeImageUrl: ''
  });

  const [errors, setErrors] = useState({});
  const navigate = useNavigate();
  const [smallImage, setSmallImage] = useState(null);
  const [largeImage, setLargeImage] = useState(null);
  const [previewSmallImage, setPreviewSmallImage] = useState(null);
  const [previewLargeImage, setPreviewLargeImage] = useState(null);
  const [types, setTypes] = useState([]);

  useEffect(() => {
    axios.get('http://localhost:8081/api/public/types').then(r => setTypes(r.data));

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
    const newErrors = {};

    const requiredFields = [
      { field: 'nameVN', label: 'Tên phim (VN)' },
      { field: 'nameEN', label: 'Tên phim (EN)' },
      { field: 'duration', label: 'Thời lượng' },
      { field: 'ageLimit', label: 'Giới hạn tuổi' },
      { field: 'fromDate', label: 'Ngày bắt đầu chiếu' },
      { field: 'toDate', label: 'Ngày kết thúc' },
      { field: 'director', label: 'Đạo diễn' },
      { field: 'actor', label: 'Diễn viên' },
      { field: 'movieProductionCompany', label: 'Hãng sản xuất' },
      { field: 'trailerLink', label: 'Trailer Link' },
      { field: 'content', label: 'Nội dung phim' },
    ];

    for (const { field, label } of requiredFields) {
      const value = form[field];
      if (value === undefined || value === null || (typeof value === 'string' && value.trim() === '')) {
        newErrors[field] = `Vui lòng nhập ${label}.`;
      }
    }

    if (form.typeIds.length === 0) newErrors.typeIds = 'Vui lòng chọn ít nhất một thể loại.';
    if (!isEdit) {
      if (!smallImage) newErrors.smallImage = 'Vui lòng chọn ảnh poster.';
      if (!largeImage) newErrors.largeImage = 'Vui lòng chọn ảnh banner.';
    }

    setErrors(newErrors);
    if (Object.keys(newErrors).length > 0) return;

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

    axios.post(url, data, { headers: { 'Content-Type': 'multipart/form-data' } })
      .then(r => {
        message.success(isEdit ? 'Cập nhật phim thành công.' : 'Thêm phim thành công.');
        if (onSuccess) onSuccess(r.data);
        navigate('/admin/movies');
      })
      .catch(err => {
        const msg = err.response?.data?.message || 'Có lỗi xảy ra khi gửi dữ liệu phim.';
        message.error(msg);
        console.error('Lỗi khi gửi phim:', err);
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
    setForm(prev => ({ ...prev, typeIds: prev.typeIds.filter(typeId => typeId !== id) }));
  };

  const renderInput = (label, name, type = 'text') => (
    <Form.Group className="mb-3">
      <Form.Label style={{ fontSize: '18px' }}>{label}</Form.Label>
      <Form.Control
        name={name}
        type={type}
        value={form[name]}
        onChange={handleChange}
        isInvalid={!!errors[name]}
      />
      <Form.Control.Feedback type="invalid">{errors[name]}</Form.Control.Feedback>
    </Form.Group>
  );

  return (
    <Form onSubmit={handleSubmit} className="p-4 border rounded shadow-sm" style={{ backgroundColor: '#ffff' }}>
      <h4 className='mb-3'>{isEdit ? 'Cập nhật phim' : 'Thêm phim mới'}</h4>
      <Row>
        <Col md={6}>
          {renderInput('Tên phim (VN)', 'nameVN')}
          {renderInput('Tên phim (EN)', 'nameEN')}
          {renderInput('Thời lượng (phút)', 'duration', 'number')}
          {renderInput('Giới hạn tuổi', 'ageLimit', 'number')}
          {renderInput('Ngày bắt đầu chiếu', 'fromDate', 'date')}
          {renderInput('Ngày kết thúc', 'toDate', 'date')}
        </Col>
        <Col md={6}>
          {renderInput('Đạo diễn', 'director')}
          {renderInput('Diễn viên', 'actor')}
          {renderInput('Hãng sản xuất', 'movieProductionCompany')}

          <Form.Group className="mb-3">
            <Form.Label>Poster</Form.Label>
            <Form.Control type="file" isInvalid={!!errors.smallImage} onChange={e => {
              const file = e.target.files[0];
              setSmallImage(file);
              if (file) setPreviewSmallImage(URL.createObjectURL(file));
            }} />
            {errors.smallImage && <div className="text-danger mt-1">{errors.smallImage}</div>}
          </Form.Group>

          <Form.Group className="mb-3">
            <Form.Label>Banner</Form.Label>
            <Form.Control type="file" isInvalid={!!errors.largeImage} onChange={e => {
              const file = e.target.files[0];
              setLargeImage(file);
              if (file) setPreviewLargeImage(URL.createObjectURL(file));
            }} />
            {errors.largeImage && <div className="text-danger mt-1">{errors.largeImage}</div>}
          </Form.Group>

          {renderInput('Trailer Link', 'trailerLink')}
        </Col>
      </Row>

      <Form.Group className="mb-3">
        <Form.Label>Nội dung phim</Form.Label>
        <Form.Control as="textarea" name="content" rows={4} value={form.content} onChange={handleChange} isInvalid={!!errors.content} />
        <Form.Control.Feedback type="invalid">{errors.content}</Form.Control.Feedback>
      </Form.Group>

      <Form.Group className="mb-3">
        <Form.Label>Thể loại</Form.Label>
        <Dropdown>
          <Dropdown.Toggle variant="light" className="w-100 text-start">
            {form.typeIds.length > 0 ? form.typeIds.map(id => {
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
            }) : <span className="text-muted">Chọn thể loại</span>}
          </Dropdown.Toggle>
          <Dropdown.Menu>
            {types.map(t => (
              <Dropdown.Item key={t.id} active={form.typeIds.includes(t.id)} onClick={() => handleSelectType(t.id)}>
                {t.name}
              </Dropdown.Item>
            ))}
          </Dropdown.Menu>
        </Dropdown>
        {errors.typeIds && <div className="text-danger mt-1">{errors.typeIds}</div>}
      </Form.Group>

      <div className='d-flex'>
        <Button type="submit" variant="primary" className='d-flex'>
          {isEdit ? <><Pencil size={16} className="me-1 mt-1" />Cập nhật</> : <><BadgePlus size={25} className="me-1" />Thêm phim</>}
        </Button>
        <Button type="button" variant="secondary" className='ms-1 d-flex' onClick={() => navigate('/admin/movies')}>
          <X className='me-1' />Hủy
        </Button>
      </div>
    </Form>
  );
}
