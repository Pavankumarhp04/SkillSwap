import { useCallback, useEffect, useState } from "react";
import api, { getErrorMessage } from "../api/axios";
import { formatDateTime } from "../utils/formatters";

const MySlots = () => {
  const [slots, setSlots] = useState([]);
  const [form, setForm] = useState({
    startTime: "",
    endTime: "",
  });

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadSlots = useCallback(async () => {
    try {
      setError("");

      const { data } = await api.get("/mentors/me/slots");

      setSlots(Array.isArray(data) ? data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadSlots();
    }, 0);

    return () => clearTimeout(timer);
  }, [loadSlots]);

  const handleChange = (e) => {
    setForm({
      ...form,
      [e.target.name]: e.target.value,
    });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();

    setError("");
    setMessage("");

    if (!form.startTime || !form.endTime) {
      setError("Please select both start and end time.");
      return;
    }

    if (new Date(form.endTime) <= new Date(form.startTime)) {
      setError("End time must be after start time.");
      return;
    }

    setSaving(true);

    try {
      await api.post("/mentors/me/slots", {
        startTime: form.startTime,
        endTime: form.endTime,
      });

      setMessage("Slot created successfully.");

      setForm({
        startTime: "",
        endTime: "",
      });

      await loadSlots();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const handleDelete = async (id) => {
    const confirmed = window.confirm(
      "Are you sure you want to delete this slot?",
    );

    if (!confirmed) {
      return;
    }

    try {
      setError("");
      setMessage("");

      await api.delete(`/mentors/me/slots/${id}`);

      setMessage("Slot deleted successfully.");

      await loadSlots();
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Slots</h1>

        <p>Create and manage your available mentoring time slots.</p>
      </div>

      {error && <div className="error">{error}</div>}

      {message && <div className="success">{message}</div>}

      <div className="profile-card">
        <h2>Create New Slot</h2>

        <form onSubmit={handleSubmit}>
          <label htmlFor="startTime">Start Time</label>

          <input
            id="startTime"
            name="startTime"
            type="datetime-local"
            value={form.startTime}
            onChange={handleChange}
            required
          />

          <label htmlFor="endTime">End Time</label>

          <input
            id="endTime"
            name="endTime"
            type="datetime-local"
            value={form.endTime}
            onChange={handleChange}
            required
          />

          <button type="submit" disabled={saving}>
            {saving ? "Creating..." : "Create Slot"}
          </button>
        </form>
      </div>

      <div className="slots-section">
        <h2>My Available Slots</h2>

        {loading ? (
          <p>Loading slots...</p>
        ) : slots.length === 0 ? (
          <div className="empty-state">
            <h3>No slots available</h3>

            <p>Create your first mentoring slot above.</p>
          </div>
        ) : (
          <div className="slot-list">
            {slots.map((slot) => (
              <div className="slot-item" key={slot.id}>
                <div>
                  <strong>{formatDateTime(slot.startTime)}</strong>

                  <p>{formatDateTime(slot.endTime)}</p>

                  <span className="muted">
                    {slot.booked ? "Booked" : "Available"}
                  </span>
                </div>

                {!slot.booked && (
                  <button type="button" onClick={() => handleDelete(slot.id)}>
                    Delete
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

export default MySlots;
