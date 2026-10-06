import { useCallback, useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api, { getErrorMessage } from "../api/axios";
import { formatRating } from "../utils/formatters";

const Mentors = () => {
  const [mentors, setMentors] = useState([]);
  const [skill, setSkill] = useState("");
  const [minRating, setMinRating] = useState("0");
  const [available, setAvailable] = useState(false);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const loadMentors = useCallback(async () => {
    try {
      setError("");

      const { data } = await api.get("/mentors", {
        params: {
          skill: skill || undefined,
          minRating: minRating !== "0" ? minRating : undefined,
          available,
          page,
          size: 9,
        },
      });

      setMentors(data.content || []);
      setTotalPages(data.totalPages || 0);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, [skill, minRating, available, page]);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadMentors();
    }, 0);

    return () => clearTimeout(timer);
  }, [loadMentors]);

  const handleSearch = (e) => {
    e.preventDefault();
    setPage(0);
  };

  return (
    <div className="page">
      <div className="page-header">
        <h1>Find Mentors</h1>

        <p>Find experienced mentors and book a session to learn new skills.</p>
      </div>

      <form className="search-bar" onSubmit={handleSearch}>
        <input
          type="text"
          value={skill}
          onChange={(e) => {
            setSkill(e.target.value);
            setPage(0);
          }}
          placeholder="Search by skill..."
        />

        <select
          value={minRating}
          onChange={(e) => {
            setMinRating(e.target.value);
            setPage(0);
          }}
        >
          <option value="0">Any rating</option>
          <option value="4">4+ ⭐</option>
          <option value="4.5">4.5+ ⭐</option>
        </select>

        <label className="checkbox-label">
          <input
            type="checkbox"
            checked={available}
            onChange={(e) => {
              setAvailable(e.target.checked);
              setPage(0);
            }}
          />
          Available only
        </label>

        <button type="submit">Search</button>
      </form>

      {error && <div className="error">{error}</div>}

      {loading ? (
        <p>Loading mentors...</p>
      ) : mentors.length === 0 ? (
        <div className="empty-state">
          <h3>No mentors found</h3>

          <p>Try changing your search filters.</p>
        </div>
      ) : (
        <>
          <div className="mentor-grid">
            {mentors.map((mentor) => (
              <div className="mentor-card" key={mentor.id}>
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

                <div className="skills">
                  {mentor.teachSkills?.length > 0 ? (
                    mentor.teachSkills.map((skillItem) => (
                      <span
                        className="skill-tag"
                        key={skillItem.id || skillItem.skillName}
                      >
                        {skillItem.skillName}
                      </span>
                    ))
                  ) : (
                    <span className="muted">No skills listed</span>
                  )}
                </div>

                <div className="mentor-meta">
                  <span>{mentor.openSlots || 0} open slots</span>
                </div>

                <Link to={`/mentors/${mentor.id}`} className="primary-link">
                  View Profile
                </Link>
              </div>
            ))}
          </div>

          {totalPages > 1 && (
            <div className="pagination">
              <button
                type="button"
                disabled={page === 0}
                onClick={() => setPage((current) => Math.max(0, current - 1))}
              >
                Previous
              </button>

              <span>
                Page {page + 1} of {totalPages}
              </span>

              <button
                type="button"
                disabled={page >= totalPages - 1}
                onClick={() =>
                  setPage((current) => Math.min(totalPages - 1, current + 1))
                }
              >
                Next
              </button>
            </div>
          )}
        </>
      )}
    </div>
  );
};

export default Mentors;
