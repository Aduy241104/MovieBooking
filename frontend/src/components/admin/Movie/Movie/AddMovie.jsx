import React, {useEffect} from 'react';
import MovieForm from './MovieForm';
import { useNavigate, useOutletContext, useLocation} from 'react-router-dom';

export default function AddMovie() {
  const navigate = useNavigate();
  const location = useLocation();
  const { setBreadcrumbItems } = useOutletContext();
  useEffect(() => {
    if (location.pathname.includes('/admin/movies/add')) {
      setBreadcrumbItems([
        { title: 'Trang chủ' },
        { title: 'Quản lý phim' },
        { title: 'Phim' },
        { title: 'Thêm phim' },
      ]);
    }
  }, [location.pathname, setBreadcrumbItems]);
  const handleSuccess = () => {
    
    navigate('/admin/movies');
  };

  return (
    <div >
      <MovieForm onSuccess={handleSuccess} />
    </div>
  );
}
