import './../node_modules/bootstrap/dist/css/bootstrap.min.css';
import 'bootstrap/dist/js/bootstrap.bundle.min.js';
import { Navigate, Route, Routes } from 'react-router-dom';
import './styles/login.css';

import LoginPage from './Page/AuthPage/LoginPage';
import SignUpPage from './Page/AuthPage/SignUpPage';
import HomePage from './Page/Home';
import { AdminLayout } from './layouts/AdminLayout/AdminLayout';
import RoomList from './components/admin/room/RoomList';
import CreateRoom from './components/admin/room/CreateRoom';
import EditRoom from './components/admin/room/EditRoom';
import RoomDetail from './components/admin/room/RoomDetail';
import { PaymentMethodPage } from './components/PaymentMethod/PaymentMethodPage';
import PaymentTransactionPage from './components/PaymentTransaction/PaymentTransactionPage';
import TypeList from './components/admin/Movie/TypeList';
import { UserPage } from './Page/admin/UserPage';
import { UserDetailPage } from './Page/admin/UserDetailPage';
import { PromotionPage } from './Page/admin/PromotionPage';
import { DashboardPage } from './Page/admin/DashboardPage';
import ReviewUI from './components/ReviewComponents/reviewcomponents';
import MovieDetail from './Page/MovieDetail/MovieDetail';
import Profile from './Page/ProfilePage/Profile/Profile';
import ChangePassword from './Page/ProfilePage/ChangePassword/ChangePassword';
import ProfileLayout from './layouts/ProfileLayout';
import { useContext } from 'react';
import { AuthContext } from './context/AuthContext';

import RequestForgotPassword from './Page/AuthPage/RequestForgotPassword';
import ResetPassword from './Page/AuthPage/ResetPassword';
import { ActivityLogPage } from './Page/admin/ActivityLogPage';

const PrivateRoute = ({ children }) => {
  const { user, isAuthLoaded } = useContext(AuthContext);

  if (!isAuthLoaded) return null; // hoặc loading spinner

  if (!user || user.role !== 'ADMIN') {
    return <Navigate to="/" replace />;
  }

  return children;
};

function App() {
  return (
    <>
      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<SignUpPage />} />
        <Route path="/" element={<HomePage />} />
        <Route path="/movie-detail/:id" element={<MovieDetail />} />
        <Route path="/forgot-password" element={<RequestForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />
        <Route path="/review/:movieId" element={<ReviewUI />} />

        {/* Profile routes */}
        <Route path="/profile" element={<ProfileLayout />}>
          <Route index element={<Profile />} />
          <Route path="password" element={<ChangePassword />} />
          <Route path="transactions" element={<Profile />} />
        </Route>

        {/* Admin routes */}
        <Route
          path="/admin"
          element={
            <PrivateRoute>
              <AdminLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<DashboardPage />} />
          <Route path="room-list" element={<RoomList />} />
          <Route path="room-list/add-room" element={<CreateRoom />} />
          <Route path="room-list/room/:id" element={<RoomDetail />} />
          <Route path="room-list/:id/edit" element={<EditRoom />} />
          <Route path="movie-type" element={<TypeList />} />
          <Route
            path="users-members"
            element={<UserPage key="members" userText="Thành viên" userFilter="CUSTOMER" />}
          />
          <Route
            path="users-members/:accountId"
            element={<UserDetailPage key="members-detail" userText="Thành viên" />}
          />
          <Route
            path="users-employees"
            element={<UserPage key="employees" userText="Nhân viên" userFilter="EMPLOYEE" />}
          />
          <Route
            path="users-employees/:accountId"
            element={<UserDetailPage key="employees-detail" userText="Nhân viên" />}
          />
          <Route
            path="promotions"
            element={<PromotionPage promotionText="Mã khuyến mãi" />}
          />
          <Route
            path="paymentmethod"
            element={<PaymentMethodPage paymentmethodText="Phương thức thanh toán" />}
          />
          <Route
            path="payment-transactions"
            element={<PaymentTransactionPage paymentmethodText="Lịch sử giao dịch" />}
          />
          <Route
            path="activity-logs"
            element={<ActivityLogPage logsText="Lịch sử hoạt động" />}
          />
        </Route>
      </Routes>
    </>
  );
}

export default App;
