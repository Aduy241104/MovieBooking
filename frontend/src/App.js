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

import RoomList from './components/admin/room/RoomList';
import CreateRoom from './components/admin/room/CreateRoom';
import EditRoom from './components/admin/room/EditRoom';
import RoomDetail from './components/admin/room/RoomDetail';


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

          {/* Tích hợp route danh sách phòng chiếu */}
          <Route path="/admin/room-list" element={<RoomList />} />
          <Route path="/admin/room-list/add-room" element={<CreateRoom />} />
          <Route path="/admin/room-list/room/:id" element={<RoomDetail />} />
          <Route path="/admin/room-list/:id/edit" element={<EditRoom />} />
        </Route>
      </Routes>
    </>

  )
}

export default App;
