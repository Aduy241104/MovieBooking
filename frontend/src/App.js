import './../node_modules/bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { Route, Routes } from 'react-router-dom';
import './styles/login.css';


import LoginPage from './Page/AuthPage/LoginPage';
import SignUpPage from './Page/AuthPage/SignUpPage';
import { AdminLayout } from './layouts/AdminLayout/AdminLayout';




import RoomList from './components/admin/room/RoomList';
import CreateRoom from './components/admin/room/CreateRoom';
import EditRoom from './components/admin/room/EditRoom';
import RoomDetail from './components/admin/room/RoomDetail';



import MovieList from './components/admin/Movie/MovieList';
import FilmDetail from './components/admin/Movie/FilmDetail';


import ShowtimeList from './components/admin/Showtime/ShowtimeList';
import TypeList from './components/admin/Movie/TypeList'


import { UserPage } from './Page/admin/UserPage';
import { UserDetailPage } from './Page/admin/UserDetailPage';
import { PromotionPage } from './Page/admin/PromotionPage';
 import { DashboardPage } from './Page/admin/DashboardPage';
import HomePage from './Page/Home/HomePage';
import MovieDetail from './Page/MovieDetail/MovieDetail';







function App() {
  return (
    <>
      <Routes>
        <Route path='/login' element={ <LoginPage /> } />
        <Route path='/register' element={ <SignUpPage /> } />
        <Route path='/' element={ <HomePage /> } />
        <Route path='/movie-detail/:id' element={ <MovieDetail /> } />


        { <Route path='/admin' element={ <AdminLayout /> }>
          <Route index element={ <DashboardPage /> } />


          <Route path='room-list' element={ <RoomList /> } />
          <Route path='room-list/add-room' element={ <CreateRoom /> } />
          <Route path='room-list/room/:id' element={ <RoomDetail /> } />
          <Route path='room-list/:id/edit' element={ <EditRoom /> } />


           <Route path='movie-list' element={ <MovieList /> } />
           <Route path='film-detail/:nameVN' element={ <FilmDetail /> } />
           <Route path='showtime-list' element={ <ShowtimeList /> } />



          <Route path='movie-type' element={<TypeList/>} />


          <Route path='users-members' element={
            <UserPage key="members" userText="Thành viên" userFilter="Member" />
          } />
          <Route path='users-members/:accountId' element={
            <UserDetailPage key="members-detail" userText="Thành viên" />
          } />


          <Route path='users-employees' element={
            <UserPage key="employees" userText="Nhân viên" userFilter="Employee" />
          } />
          <Route path='users-employees/:accountId' element={
            <UserDetailPage key="employees-detail" userText="Nhân viên" />
          } />



          <Route path='promotions' element={
            <PromotionPage promotionText="Mã khuyến mãi" />
          } />
        </Route> }



      </Routes>
    </>
  );
}


export default App;



