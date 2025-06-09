import './../node_modules/bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { Route, Routes } from 'react-router-dom';
import './login.css'
import LoginPage from './Page/LoginPage';
import SignUpPage from './Page/SignUpPage';
import DefaultLayout from './layouts/DefaultLayout/DefaultLayout';
import ReviewComponents from './components/ReviewComponents';







function App() {


  return (
    <>
      <Routes>
        <Route path='/' element={<LoginPage />} />
        <Route path='/review' element={<ReviewComponents />} />
        <Route path='/register' element={<SignUpPage />} />
        <Route path='/home' element={<DefaultLayout />} />
      </Routes>
    </>

  )
}

export default App;
