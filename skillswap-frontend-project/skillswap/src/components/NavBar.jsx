import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../context/useAuth";
import { ROLES } from "../utils/constants";

export default function Navbar() {
  const { user, isAuthenticated, credits, logout } = useAuth();

  const navigate = useNavigate();

  function handleLogout() {
    logout();
    navigate("/login");
  }

  return (
    <nav className="navbar">
      <Link to="/" className="brand">
        SkillSwap
      </Link>

      <div className="nav-links">
        {isAuthenticated ? (
          <>
            <Link to="/">Profile</Link>

            <Link to="/mentors">Find mentors</Link>

            <Link to="/sessions">Sessions</Link>

            {user?.role === ROLES.MENTOR && (
              <Link to="/my-slots">My slots</Link>
            )}

            {user?.role === ROLES.ADMIN && <Link to="/admin">Admin</Link>}

            {credits !== null && credits !== undefined && (
              <Link to="/wallet" className="credit-badge">
                {credits} {credits === 1 ? "credit" : "credits"}
              </Link>
            )}

            <span className="muted">{user?.name}</span>

            <button className="btn btn-small" onClick={handleLogout}>
              Log out
            </button>
          </>
        ) : (
          <>
            <Link to="/login">Log in</Link>

            <Link to="/register">Register</Link>
          </>
        )}
      </div>
    </nav>
  );
}
