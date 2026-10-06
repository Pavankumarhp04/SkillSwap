import { useCallback, useEffect, useState } from "react";
import api, { getErrorMessage } from "../api/axios";
import { useAuth } from "../context/useAuth";
import { ROLES } from "../utils/constants";

const Profile = () => {
  const { user, updateName } = useAuth();

  const [profile, setProfile] = useState(null);
  const [skills, setSkills] = useState([]);

  const [name, setName] = useState("");
  const [skillName, setSkillName] = useState("");
  const [skillType, setSkillType] = useState("TEACH");

  const [loading, setLoading] = useState(true);
  const [savingName, setSavingName] = useState(false);
  const [addingSkill, setAddingSkill] = useState(false);

  const [error, setError] = useState("");
  const [message, setMessage] = useState("");

  const loadProfile = useCallback(async () => {
    try {
      setError("");

      const [profileResponse, skillsResponse] = await Promise.all([
        api.get("/users/me"),
        api.get("/users/me/skills"),
      ]);

      setProfile(profileResponse.data);
      setName(profileResponse.data?.name || "");
      setSkills(Array.isArray(skillsResponse.data) ? skillsResponse.data : []);
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      loadProfile();
    }, 0);

    return () => clearTimeout(timer);
  }, [loadProfile]);

  const handleUpdateName = async (e) => {
    e.preventDefault();

    if (!name.trim()) {
      setError("Name cannot be empty.");
      return;
    }

    try {
      setError("");
      setMessage("");
      setSavingName(true);

      await api.put("/users/me", {
        name: name.trim(),
      });

      if (updateName) {
        updateName(name.trim());
      }

      setProfile((current) => ({
        ...current,
        name: name.trim(),
      }));

      setMessage("Name updated successfully.");
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setSavingName(false);
    }
  };

  const handleAddSkill = async (e) => {
    e.preventDefault();

    if (!skillName.trim()) {
      setError("Please enter a skill name.");
      return;
    }

    try {
      setError("");
      setMessage("");
      setAddingSkill(true);

      await api.post("/users/me/skills", {
        skillName: skillName.trim(),
        type: skillType,
      });

      setSkillName("");
      setMessage("Skill added successfully.");

      await loadProfile();
    } catch (err) {
      setError(getErrorMessage(err));
    } finally {
      setAddingSkill(false);
    }
  };

  const handleDeleteSkill = async (skillId) => {
    try {
      setError("");
      setMessage("");

      await api.delete(`/users/me/skills/${skillId}`);

      setSkills((current) => current.filter((skill) => skill.id !== skillId));

      setMessage("Skill deleted successfully.");
    } catch (err) {
      setError(getErrorMessage(err));
    }
  };

  if (loading) {
    return (
      <div className="page">
        <p>Loading profile...</p>
      </div>
    );
  }

  return (
    <div className="page">
      <div className="page-header">
        <h1>My Profile</h1>

        <p>Manage your SkillSwap profile and skills.</p>
      </div>

      {error && <div className="error">{error}</div>}

      {message && <div className="success">{message}</div>}

      <div className="profile-card">
        <h2>Profile Information</h2>

        <div className="profile-info">
          <p>
            <strong>Email:</strong> {profile?.email || user?.email || "—"}
          </p>

          <p>
            <strong>Role:</strong> {profile?.role || user?.role || "—"}
          </p>

          <p>
            <strong>User ID:</strong> {profile?.id || user?.id || "—"}
          </p>
        </div>

        <form onSubmit={handleUpdateName}>
          <label htmlFor="name">Full Name</label>

          <input
            id="name"
            type="text"
            value={name}
            onChange={(e) => setName(e.target.value)}
            placeholder="Enter your name"
            required
          />

          <button type="submit" disabled={savingName}>
            {savingName ? "Saving..." : "Update Name"}
          </button>
        </form>
      </div>

      <div className="profile-card">
        <h2>Add Skill</h2>

        <form onSubmit={handleAddSkill}>
          <label htmlFor="skillName">Skill Name</label>

          <input
            id="skillName"
            type="text"
            value={skillName}
            onChange={(e) => setSkillName(e.target.value)}
            placeholder="Example: Java"
            required
          />

          <label htmlFor="skillType">Skill Type</label>

          <select
            id="skillType"
            value={skillType}
            onChange={(e) => setSkillType(e.target.value)}
          >
            <option value="TEACH">I can teach</option>

            <option value="LEARN">I want to learn</option>
          </select>

          <button type="submit" disabled={addingSkill}>
            {addingSkill ? "Adding..." : "Add Skill"}
          </button>
        </form>
      </div>

      <div className="profile-card">
        <h2>My Skills</h2>

        {skills.length === 0 ? (
          <div className="empty-state">
            <p>You haven't added any skills yet.</p>
          </div>
        ) : (
          <>
            <h3>Skills I Teach</h3>

            <div className="skills">
              {skills
                .filter((skill) => skill.type === "TEACH")
                .map((skill) => (
                  <div className="skill-tag" key={skill.id}>
                    {skill.skillName}

                    <button
                      type="button"
                      onClick={() => handleDeleteSkill(skill.id)}
                    >
                      ×
                    </button>
                  </div>
                ))}
            </div>

            <h3>Skills I Want to Learn</h3>

            <div className="skills">
              {skills
                .filter((skill) => skill.type === "LEARN")
                .map((skill) => (
                  <div className="skill-tag" key={skill.id}>
                    {skill.skillName}

                    <button
                      type="button"
                      onClick={() => handleDeleteSkill(skill.id)}
                    >
                      ×
                    </button>
                  </div>
                ))}
            </div>
          </>
        )}
      </div>

      {user?.role === ROLES.MENTOR && (
        <div className="profile-card">
          <h2>Mentor Account</h2>

          <p>
            You can create mentoring slots from the <strong>My Slots</strong>{" "}
            page.
          </p>
        </div>
      )}
    </div>
  );
};
export default Profile;
