import { useCallback, useEffect, useState } from "react";
import { Link, useParams } from "react-router-dom";
import api, { getErrorMessage } from "../api/axios";
import { formatDateTime, formatRating } from "../utils/formatters";

const MentorDetail = () => {
  const { id } = useParams();

  const [mentor, setMentor] = useState(null);
  const [slots, setSlots] = useState([]);

  const [loading, setLoading] = useState(true);
  const [bookingId, setBookingId] = useState(null);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadMentor = useCallback(async () => {
    try {
      setError("");

      const { data } = await api.get(`/mentors/${id}`);

      setMentor(data.mentor);
      setSlots(data.openSlots || []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [id]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadMentor();
    }, 0);

    return () => clearTimeout(timer);
  }, [loadMentor]);

  const handleBook = async (slotId) => {
    try {
      setError("");
      setMessage("");
      setBookingId(slotId);

      await api.post("/sessions", {
        slotId: slotId,
      });

      setMessage("Session booked successfully.");

      await loadMentor();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setBookingId(null);
    }
  };

  if (loading) {
    return (
      <div className="page">
        <p>Loading mentor...</p>
      </div>
    );
  }

  if (!mentor) {
    return (
      <div className="page">
        <div className="error">Mentor not found.</div>

        <Link to="/mentors" className="primary-link">
          Back to mentors
        </Link>
      </div>
    );
  }

  return (
    <div className="mentor-detail-page">
      <div className="page-header">
        <Link to="/mentors" className="primary-link">
          ← Back to mentors
        </Link>

        <h1>{mentor.name}</h1>

        <p>View mentor details and book an available session.</p>
      </div>

      {error && <div className="error">{error}</div>}

      {message && <div className="success">{message}</div>}

      <div className="mentor-profile-card">
        <div className="mentor-card-top">
          <div className="avatar">
            {mentor.name?.charAt(0)?.toUpperCase() || "M"}
          </div>

          <div>
            <h2>{mentor.name}</h2>

            <p className="muted">
              {formatRating(mentor.averageRating)}
              {" · "}
              {mentor.reviewCount || 0} reviews
            </p>
          </div>
        </div>

        <h3>Skills I Teach</h3>

        <div className="skills">
          {mentor.teachSkills?.length > 0 ? (
            mentor.teachSkills.map((skill) => (
              <span className="skill-tag" key={skill.id || skill.skillName}>
                {skill.skillName}
              </span>
            ))
          ) : (
            <span className="muted">No skills listed.</span>
          )}
        </div>
      </div>

      <div className="slots-section">
        <h2>Available Slots</h2>

        {slots.length === 0 ? (
          <div className="empty-state">
            <h3>No available slots</h3>

            <p>This mentor currently has no open sessions.</p>
          </div>
        ) : (
          <div className="slot-grid">
            {slots.map((slot) => (
              <div className="slot-card" key={slot.id}>
                <h3>{formatDateTime(slot.startTime)}</h3>

                <p>Until {formatDateTime(slot.endTime)}</p>

                {slot.booked ? (
                  <button type="button" disabled>
                    Already Booked
                  </button>
                ) : (
                  <button
                    type="button"
                    disabled={bookingId === slot.id}
                    onClick={() => handleBook(slot.id)}
                  >
                    {bookingId === slot.id ? "Booking..." : "Book Session"}
                  </button>
                )}
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default MentorDetail;
