import { useContext, useState, useEffect } from 'react'
import Header from './Header'
import { AuthContext } from '../../context/AuthContext';
import GoToTop from '../../components/GoToTop/GoToTop';
import Footer from './Footer/Footer';

function DefaultLayout({ children }) {
  const { user, logout } = useContext(AuthContext);
  const [isLogin, setLogin] = useState(user);

  useEffect(() => {
    setLogin(user);
  }, [user]);

  return (
    <div className='bg-midnight position-relative'>
      <Header user={ isLogin } logout={ logout } />
      { children }
      <GoToTop />
      <Footer />
    </div>
  )
}

export default DefaultLayout