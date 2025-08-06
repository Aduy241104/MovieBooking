import React, {useEffect} from 'react';
import MovieForm from './MovieForm';
import { useNavigate, useParams, useLocation, useOutletContext } from 'react-router-dom';

export default function EditMovie() {
  const { id } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();
  useEffect(() => {
    if (location.pathname.includes('/admin/movies/edit')) {
      setBreadcrumbItems([
        { title: 'Trang chủ' },
        { title: 'Quản lý phim' },
        { title: 'Phim', href:"/admin/movies" },
        { title: 'Sửa phim' },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);
  const handleSuccess = () => {
   
    navigate('/admin/movies');
  };

  return (
    <div className="">
      <MovieForm movieId={id} onSuccess={handleSuccess} />
    </div>
  );
}
