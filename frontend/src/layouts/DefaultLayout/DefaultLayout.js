import React, { useContext, useState, useEffect } from 'react'
import Header from './Header'
import HomePage from '../../Page/Home/HomePage'
import { AuthContext } from '../../context/AuthContext';

function DefaultLayout({ children }) {
  const { user } = useContext(AuthContext);
  const [isLogin, setLogin] = useState(user);

  useEffect(() => {
    setLogin(user);
  }, [user]);

  return (
    <div className='bg-midnight'>
      <Header user={ isLogin } />
      { children }
    </div>
  )
}

export default DefaultLayout