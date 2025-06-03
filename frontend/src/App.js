import './../node_modules/bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { Route, Routes } from 'react-router-dom';
import './login.css'
import LoginPage from './Page/AuthPage/LoginPage';
import SignUpPage from './Page/AuthPage/SignUpPage';
import DefaultLayout from './layouts/DefaultLayout/DefaultLayout';







function App() {


  return (
    <>
      <Routes>
        <Route path='/login' element={ <LoginPage /> } />

        <Route path='/register' element={ <SignUpPage /> } />
        <Route path='/' element={ <DefaultLayout /> } />
      </Routes>
    </>

  )
}

export default App;
