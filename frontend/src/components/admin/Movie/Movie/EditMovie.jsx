import React from 'react';
import MovieForm from './MovieForm';
import { useNavigate, useParams } from 'react-router-dom';

export default function EditMovie() {
  const { id } = useParams();
  const navigate = useNavigate();

  const handleSuccess = () => {
    alert('Cập nhật phim thành công!');
    navigate('/admin/movies');
  };

  return (
    <div className="container mt-4">
      <MovieForm movieId={id} onSuccess={handleSuccess} />
    </div>
  );
}
