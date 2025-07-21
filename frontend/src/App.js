import "./../node_modules/bootstrap/dist/css/bootstrap.min.css";
import "bootstrap/dist/js/bootstrap.bundle.min.js";
import { Navigate, Route, Routes, useLocation } from "react-router-dom";
import "./styles/login.css";
import { useContext } from "react";
import { AuthContext } from "./context/AuthContext";
import { NotificationProvider } from "./context/NotificationContext";

// Auth
import LoginPage from "./Page/AuthPage/LoginPage";
import SignUpPage from "./Page/AuthPage/SignUpPage";
import RequestForgotPassword from "./Page/AuthPage/RequestForgotPassword";
import ResetPassword from "./Page/AuthPage/ResetPassword";

// Layouts
import { AdminLayout } from "./layouts/AdminLayout/AdminLayout";
import { EmployeeLayout } from "./layouts/EmployeeLayout/EmployeeLayout";
import ProfileLayout from "./layouts/ProfileLayout";

// Public pages
import HomePage from "./Page/Home/HomePage";
import MovieDetail from "./Page/MovieDetail/MovieDetail";

// Admin: Movie
import MovieList from "./components/admin/Movie/Movie/MovieList";
import AddMovie from "./components/admin/Movie/Movie/AddMovie";
import EditMovie from "./components/admin/Movie/Movie/EditMovie";
import TypeList from "./components/admin/Movie/MovieType/TypeList";
import FilmDetail from "./components/admin/Movie/Movie/FilmDetail";

// Admin: Room
import RoomList from "./components/admin/Room/RoomList";
import CreateRoom from "./components/admin/Room/CreateRoom";
import EditRoom from "./components/admin/Room/EditRoom";
import RoomDetail from "./components/admin/Room/RoomDetail";

// Admin: Showtime, FareType, Booking, Review
import ShowtimeList from "./components/admin/Showtime/ShowtimeList";
import FareTypeList from "./components/admin/TicketPrice/FareTypeList";
import BookingList from "./components/admin/TicketPrice/BookingList";
import BookingDetail from "./components/admin/TicketPrice/BookingDetail";
import ReviewList from "./components/admin/Review/ReviewList";
import ReviewDetail from "./components/admin/Review/ReviewDetail";

// Admin: Users, Promotion, Logs
import { UserPage } from "./Page/admin/UserPage";
import { UserDetailPage } from "./Page/admin/UserDetailPage";
import { PromotionPage } from "./Page/admin/PromotionPage";
import { DashboardPage } from "./Page/admin/DashboardPage";
import { ActivityLogPage } from "./Page/admin/ActivityLogPage";

// User: Profile
import Profile from "./Page/ProfilePage/Profile/Profile";
import ChangePassword from "./Page/ProfilePage/ChangePassword/ChangePassword";
import { Notification } from "./Page/ProfilePage/Notification/Notification";

// Booking
import BookingPage from "./Page/Booking/BookingPage";
import BookingSuccessPage from "./Page/Booking/BookingSuccess/BookingSuccessPage";
import BookingFailurePage from "./Page/Booking/BookingFailure/BookingFailurePage";
import BookingHistoryPage from "./Page/Booking/BookingHistory/BookingHistoryPage";
import BookingDetailPage from "./Page/Booking/BookingDetail/BookingDetailPage";

// Route bảo vệ
const PrivateRoute = ({ children }) => {
  const { user, isAuthLoaded } = useContext(AuthContext);
  if (!isAuthLoaded) return <div>Loading authentication state...</div>;
  if (!user || user.role !== "ADMIN") {
    return <Navigate to="/" replace />;
  }
  return children;
};

const ProtectedRoute = ({ children }) => {
  const { user, isAuthLoaded } = useContext(AuthContext);
  const location = useLocation();
  if (!isAuthLoaded) return <div>Loading authentication state...</div>;
  if (!user) {
    console.log("ProtectedRoute (in App.js) - No user after auth loaded, redirecting to /login");
    return <Navigate to="/login" state={{ from: location }} replace />;
  }
  return children;
};

function App() {
  return (
    <NotificationProvider>
      <Routes>
        {/* Auth */}
        <Route path="/login" element={<LoginPage />} />
        <Route path="/register" element={<SignUpPage />} />
        <Route path="/forgot-password" element={<RequestForgotPassword />} />
        <Route path="/reset-password" element={<ResetPassword />} />

        {/* Public */}
        <Route path="/" element={<HomePage />} />
        <Route path="/movie-detail/:id" element={<MovieDetail />} />

        {/* Profile routes */}
        <Route path="/profile" element={<ProfileLayout />}>
          <Route index element={<Profile />} />
          <Route path="password" element={<ChangePassword />} />
          <Route path="transactions" element={<Profile />} />
          <Route path="notifications" element={<Notification />} />
        </Route>
        <Route element={<ProfileLayout />}>
          <Route path="/booking/history" element={<BookingHistoryPage />} />
          <Route path="/booking/details/:bookingId" element={<BookingDetailPage />} />
        </Route>
        {/* Booking */}
        <Route
          path="/booking"
          element={
            <ProtectedRoute>
              <BookingPage />
            </ProtectedRoute>
          }
        />
        <Route
          path="/booking/success"
          element={
            <ProtectedRoute>
              <BookingSuccessPage />
            </ProtectedRoute>
          }
        />
        <Route path="/booking/failure" element={<BookingFailurePage />} />
        <Route
          path="/booking/history"
          element={
            <ProtectedRoute>
              <ProfileLayout>
                <BookingHistoryPage />
              </ProfileLayout>
            </ProtectedRoute>
          }
        />
        <Route
          path="/booking/details/:bookingId"
          element={
            <ProtectedRoute>
              <ProfileLayout>
                <BookingDetailPage />
              </ProfileLayout>
            </ProtectedRoute>
          }
        />

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

          {/* Room */}
          <Route path="room-list" element={<RoomList />} />
          <Route path="room-list/add-room" element={<CreateRoom />} />
          <Route path="room-list/room/:id" element={<RoomDetail />} />
          <Route path="room-list/room/edit/:id" element={<EditRoom />} />

          {/* Movie */}
          <Route path="movie-type" element={<TypeList />} />
          <Route path="movies" element={<MovieList />} />
          <Route path="movies/add" element={<AddMovie />} />
          <Route path="movies/edit/:id" element={<EditMovie />} />
          <Route path="film-detail/:id" element={<FilmDetail />} />

          {/* Showtime */}
          <Route path="showtime-list" element={<ShowtimeList />} />

          {/* Fare & Booking */}
          <Route path="faretype-list" element={<FareTypeList />} />
          <Route path="booking-list" element={<BookingList />} />
          <Route path="booking-detail/:movieId" element={<BookingDetail />} />

          {/* Review */}
          <Route path="review-list" element={<ReviewList />} />
          <Route path="review-detail/:movieId" element={<ReviewDetail />} />

          {/* Customer routes */}
          <Route
            path="users-members"
            element={<UserPage key="members" userText="Thành viên" userFilter="CUSTOMER" />}
          />
          <Route
            path="users-members/:accountId"
            element={<UserDetailPage key="members-detail" userText="Thành viên" />}
          />

          {/* Employee routes */}
          <Route
            path="users-employees"
            element={<UserPage key="employees" userText="Nhân viên" userFilter="EMPLOYEE" />}
          />
          <Route
            path="users-employees/:accountId"
            element={<UserDetailPage key="employees-detail" userText="Nhân viên" />}
          />

          {/* Promotion */}
          <Route path="promotions" element={<PromotionPage promotionText="Mã khuyến mãi" />} />

          {/* Activity Log */}
          <Route path="activity-logs" element={<ActivityLogPage logsText="Lịch sử hoạt động" />} />
        </Route>

        {/* Employee routes */}
        <Route
          path="/employee"
          element={
            <PrivateRoute>
              <EmployeeLayout />
            </PrivateRoute>
          }
        >
          <Route index element={<h1>Employee Dashboard</h1>} />
          {/* Add employee-specific routes here */}
        </Route>
      </Routes>
    </NotificationProvider>
  );
}

export default App;