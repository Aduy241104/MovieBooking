import React from 'react';
import MovieForm from './MovieForm';
import { useNavigate } from 'react-router-dom';

export default function AddMovie() {
  const navigate = useNavigate();

  const handleSuccess = () => {
    alert('Thêm phim thành công!');
    navigate('/admin/movies'); // Hoặc route tuỳ bạn
  };

  return (
    <div className="container mt-4">
      <MovieForm onSuccess={handleSuccess} />
    </div>
  );
}
