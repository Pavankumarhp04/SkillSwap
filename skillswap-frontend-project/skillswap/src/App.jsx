import { Navigate, Route, Routes } from "react-router-dom";

import { AuthProvider } from "./context/AuthContext";

import NavBar from "./components/NavBar";
import ProtectedRoute from "./components/ProtectedRoute";

import Login from "./pages/Login";
import Register from "./pages/Register";
import Profile from "./pages/Profile";
import Mentors from "./pages/Mentors";
import MentorDetail from "./pages/MentorDetail";
import MySlots from "./pages/MySlots";
import Sessions from "./pages/Sessions";
import Wallet from "./pages/Wallet";
import AdminDashboard from "./pages/AdminDashboard";

const App = () => {
  return (
    <AuthProvider>
      <NavBar />

      <Routes>
        {/* Public routes */}
        <Route path="/login" element={<Login />} />

        <Route path="/register" element={<Register />} />

        {/* Logged-in users */}
        <Route element={<ProtectedRoute />}>
          <Route path="/" element={<Profile />} />

          <Route path="/profile" element={<Profile />} />

          <Route path="/mentors" element={<Mentors />} />

          <Route path="/mentors/:id" element={<MentorDetail />} />

          <Route path="/sessions" element={<Sessions />} />

          <Route path="/wallet" element={<Wallet />} />
        </Route>

        {/* Mentor only */}
        <Route element={<ProtectedRoute roles={["MENTOR"]} />}>
          <Route path="/my-slots" element={<MySlots />} />
        </Route>

        {/* Admin only */}
        <Route element={<ProtectedRoute roles={["ADMIN"]} />}>
          <Route path="/admin" element={<AdminDashboard />} />
        </Route>

        {/* Unknown URL */}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </AuthProvider>
  );
};

export default App;
