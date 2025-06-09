import './../node_modules/bootstrap/dist/css/bootstrap.min.css'
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { Route, Routes } from 'react-router-dom';
import './login.css'
import LoginPage from './Page/AuthPage/LoginPage';
import SignUpPage from './Page/AuthPage/SignUpPage';
import { AdminLayout } from './layouts/AdminLayout/AdminLayout';

import RoomList from './components/admin/room/RoomList';
import CreateRoom from './components/admin/room/CreateRoom';
import EditRoom from './components/admin/room/EditRoom';
import RoomDetail from './components/admin/room/RoomDetail';

import { UserPage } from './Page/admin/UserPage';
import { UserDetailPage } from './Page/admin/UserDetailPage';
import { PromotionPage } from './Page/admin/PromotionPage';
import { DashboardPage } from './Page/admin/DashboardPage';

import HomePage from './Page/Home'




function App() {

  return (
    <>
      <Routes>
        <Route path='/login' element={ <LoginPage /> } />
        <Route path='/register' element={ <SignUpPage /> } />
        <Route path='/' element={ <HomePage /> } />

        <Route path='/register' element={ <SignUpPage /> } />


        <Route path='/admin' element={ <AdminLayout /> }>
          <Route index element={ <DashboardPage /> } />

          <Route path='/admin' element={ <AdminLayout /> }>

            {/* Tích hợp route danh sách phòng chiếu */ }
            <Route path="/admin/room-list" element={ <RoomList /> } />
            <Route path="/admin/room-list/add-room" element={ <CreateRoom /> } />
            <Route path="/admin/room-list/room/:id" element={ <RoomDetail /> } />
            <Route path="/admin/room-list/:id/edit" element={ <EditRoom /> } />

            <Route index element={ <DashboardPage /> } />


            <Route path='users-members'
              element={ <UserPage key="members" userText="Thành viên" userFilter="Member" /> } />
            <Route path='users-members/:accountId'
              element={ <UserDetailPage key="members-detail" userText="Thành viên" /> } />

            <Route path='users-employees'
              element={ <UserPage key="employees" userText="Nhân viên" userFilter="Employee" /> } />
            <Route path='users-employees/:accountId'
              element={ <UserDetailPage key="employees-detail" userText="Nhân viên" /> } />

            <Route path='promotions'
              element={ <PromotionPage promotionText="Mã khuyến mãi" /> } />
          </Route>
        </Route>
      </Routes>
    </>

  )
}

export default App;
