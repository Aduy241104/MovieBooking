import { useContext, useState, useEffect } from 'react'
import Header from './Header'
import { AuthContext } from '../../context/AuthContext';

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
    </div>
  )
}

export default DefaultLayout