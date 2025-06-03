import './../node_modules/bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { Route, Routes } from 'react-router-dom';
import './login.css'
import LoginPage from './Page/LoginPage';
import SignUpPage from './Page/SignUpPage';
import DefaultLayout from './layouts/DefaultLayout/DefaultLayout';
import { Dashboard } from './Page/admin/Dashboard';
import { AdminLayout } from './layouts/AdminLayout/AdminLayout';
import { MemberPage } from './Page/admin/MemberPage';
import { EmployeePage } from './Page/admin/EmployeePage';







function App() {


  return (
    <>
      <Routes>
        <Route path='/' element={<LoginPage />} />

        <Route path='/register' element={<SignUpPage />} />
        <Route path='/home' element={<DefaultLayout />} />

        <Route path='/admin' element={<AdminLayout />}>
          <Route index element={<Dashboard />} />
          <Route path='users-members' element={<MemberPage />} />
          <Route path='users-employees' element={<EmployeePage />} />
        </Route>
      </Routes>
    </>

  )
}

export default App;
