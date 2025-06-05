import './../node_modules/bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { Route, Routes } from 'react-router-dom';
import './login.css'
import LoginPage from './Page/LoginPage';
import SignUpPage from './Page/SignUpPage';
import DefaultLayout from './layouts/DefaultLayout/DefaultLayout';
import { Dashboard } from './Page/admin/Dashboard';
import { AdminLayout } from './layouts/AdminLayout/AdminLayout';
import { UserPage } from './Page/admin/UserPage';
import { UserDetailPage } from './Page/admin/UserDetailPage';



function App() {

  return (
    <>
      <Routes>
        <Route path='/' element={<LoginPage />} />

        <Route path='/register' element={<SignUpPage />} />
        <Route path='/home' element={<DefaultLayout />} />

        <Route path='/admin' element={<AdminLayout />}>
          <Route index element={<Dashboard />} />

          <Route path='users-members'
            element={<UserPage key="members" userText="Thành viên" userFilter="Member" />} />
          <Route path='users-members/:accountId'
            element={<UserDetailPage key="members-detail" userText="Thành viên" />} />

          <Route path='users-employees'
            element={<UserPage key="employees" userText="Nhân viên" userFilter="Employee" />} />
          <Route path='users-employees/:accountId'
            element={<UserDetailPage key="employees-detail" userText="Nhân viên" />} />
        </Route>
      </Routes>
    </>

  )
}

export default App;
