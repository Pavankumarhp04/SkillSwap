import { useCallback, useEffect, useState } from "react";
import api, { getErrorMessage } from "../api/axios";
import { formatDateTime } from "../utils/formatters";
import { ROLES } from "../utils/constants";

const AdminDashboard = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadUsers = useCallback(async () => {
    try {
      setError("");

      const { data } = await api.get("/admin/users");

      setUsers(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadUsers();
    }, 0);

    return () => clearTimeout(timer);
  }, [loadUsers]);

  return (
    <div className="page">
      <div className="page-header">
        <h1>Admin Dashboard</h1>
        <p>Manage and view SkillSwap users.</p>
      </div>

      {error && <div className="error">{error}</div>}

      {loading ? (
        <p>Loading users...</p>
      ) : users.length === 0 ? (
        <div className="empty-state">
          <h3>No users found</h3>
          <p>There are no users to display.</p>
        </div>
      ) : (
        <div className="admin-table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>ID</th>
                <th>Name</th>
                <th>Email</th>
                <th>Role</th>
                <th>Credits</th>
                <th>Created</th>
              </tr>
            </thead>

            <tbody>
              {users.map((item) => (
                <tr key={item.id}>
                  <td>{item.id}</td>

                  <td>{item.name || "—"}</td>

                  <td>{item.email || "—"}</td>

                  <td>
                    <span className="role-badge">
                      {item.role || ROLES.LEARNER}
                    </span>
                  </td>

                  <td>{item.credits ?? 0}</td>

                  <td>{formatDateTime(item.createdAt || item.createdDate)}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
};

export default AdminDashboard;
