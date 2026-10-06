import { useCallback, useEffect, useState } from "react";

import api, { getErrorMessage } from "../api/axios";
import { useAuth } from "../context/useAuth";
import { formatDateTime } from "../utils/formatters";

const Sessions = () => {
  const { user, refreshCredits } = useAuth();

  const [sessions, setSessions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadSessions = useCallback(async () => {
    try {
      setError("");

      const { data } = await api.get("/sessions/me");

      setSessions(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadSessions();
    }, 0);

    return () => clearTimeout(timer);
  }, [loadSessions]);

  const completeSession = async (id) => {
    try {
      setError("");
      setMessage("");

      await api.post(`/sessions/${id}/complete`);

      setMessage("Session completed successfully.");

      await loadSessions();

      if (refreshCredits) {
        await refreshCredits();
      }
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Sessions</h1>

        <p>View your learning and mentoring sessions.</p>
      </div>

      {error && <div className="error">{error}</div>}

      {message && <div className="success">{message}</div>}

      {loading ? (
        <p>Loading sessions...</p>
      ) : sessions.length === 0 ? (
        <div className="empty-state">
          <h3>No sessions yet</h3>

          <p>Book a mentor session to get started.</p>
        </div>
      ) : (
        <div className="session-list">
          {sessions.map((session) => {
            const isMentor = user?.role === "MENTOR";

            const status = session.status?.toLowerCase();

            return (
              <div className="session-card" key={session.id}>
                <div className="session-header">
                  <h2>Session #{session.id}</h2>

                  <span className={`status status-${status}`}>
                    {session.status}
                  </span>
                </div>

                <div className="session-details">
                  <p>
                    <strong>Mentor:</strong>{" "}
                    {session.mentorName || session.mentor?.name || "—"}
                  </p>

                  <p>
                    <strong>Learner:</strong>{" "}
                    {session.learnerName || session.learner?.name || "—"}
                  </p>

                  <p>
                    <strong>Start:</strong>{" "}
                    {formatDateTime(session.startTime || session.slotStartTime)}
                  </p>

                  <p>
                    <strong>End:</strong>{" "}
                    {formatDateTime(session.endTime || session.slotEndTime)}
                  </p>
                </div>

                {session.meetingLink && (
                  <a
                    href={session.meetingLink}
                    target="_blank"
                    rel="noreferrer"
                    className="primary-link"
                  >
                    Join Meeting
                  </a>
                )}

                {isMentor && session.status === "BOOKED" && (
                  <button
                    type="button"
                    onClick={() => completeSession(session.id)}
                  >
                    Complete Session
                  </button>
                )}
              </div>
            );
          })}
        </div>
      )}
    </div>
  );
};

export default Sessions;
