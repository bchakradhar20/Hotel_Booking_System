import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import Navbar from './components/Navbar'
import ProtectedRoute from './components/ProtectedRoute'
import AdminRoute from './components/AdminRoute'
import Login from './pages/Login'
import Register from './pages/Register'
import Rooms from './pages/Rooms'
import BookRoom from './pages/BookRoom'
import MyReservations from './pages/MyReservations'
import Profile from './pages/Profile'
import AdminDashboard from './pages/AdminDashboard'
import ManageRooms from './pages/ManageRooms'
import ManageReservations from './pages/ManageReservations'
import Users from './pages/Users'
import CreateAdmin from './pages/CreateAdmin'

export default function App() {
  return (
    <BrowserRouter>
      <Navbar />
      <div className="container py-4">
        <Routes>
          <Route path="/" element={<Navigate to="/rooms" />} />
          <Route path="/login" element={<Login />} />
          <Route path="/register" element={<Register />} />
          <Route path="/rooms" element={<Rooms />} />
          <Route path="/book/:roomId" element={<ProtectedRoute><BookRoom /></ProtectedRoute>} />
          <Route path="/my-reservations" element={<ProtectedRoute><MyReservations /></ProtectedRoute>} />
          <Route path="/profile" element={<ProtectedRoute><Profile /></ProtectedRoute>} />
          <Route path="/admin" element={<AdminRoute><AdminDashboard /></AdminRoute>} />
          <Route path="/admin/rooms" element={<AdminRoute><ManageRooms /></AdminRoute>} />
          <Route path="/admin/reservations" element={<AdminRoute><ManageReservations /></AdminRoute>} />
          <Route path="/admin/users" element={<AdminRoute><Users /></AdminRoute>} />
          <Route path="/admin/create-admin" element={<AdminRoute><CreateAdmin /></AdminRoute>} />
        </Routes>
      </div>
    </BrowserRouter>
  )
}
