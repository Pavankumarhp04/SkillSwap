import { useCallback, useEffect, useMemo, useState } from "react";

import api from "../api/axios";
import { AuthContext } from "./AuthContextValue";

export function AuthProvider({ children }) {
  const [token, setToken] = useState(() =>
    localStorage.getItem("skillswap_token"),
  );

  const [user, setUser] = useState(() => {
    const savedUser = localStorage.getItem("skillswap_user");

    try {
      return savedUser ? JSON.parse(savedUser) : null;
    } catch {
      return null;
    }
  });

  const [credits, setCredits] = useState(null);
  const [loading, setLoading] = useState(true);

  const isAuthenticated = Boolean(token);

  const saveAuth = useCallback((authData) => {
    localStorage.setItem("skillswap_token", authData.token);

    localStorage.setItem(
      "skillswap_user",
      JSON.stringify({
        id: authData.userId,
        name: authData.name,
        email: authData.email,
        role: authData.role,
      }),
    );

    setToken(authData.token);

    setUser({
      id: authData.userId,
      name: authData.name,
      email: authData.email,
      role: authData.role,
    });
  }, []);

  const clearAuth = useCallback(() => {
    localStorage.removeItem("skillswap_token");
    localStorage.removeItem("skillswap_user");

    setToken(null);
    setUser(null);
    setCredits(null);
  }, []);

  const refreshCredits = useCallback(async () => {
    if (!token) {
      setCredits(null);
      return;
    }

    try {
      const { data } = await api.get("/users/me");

      setCredits(data?.credits ?? 0);

      setUser((current) => {
        if (!current) {
          return current;
        }

        const updatedUser = {
          ...current,
          id: data?.id ?? current.id,
          name: data?.name ?? current.name,
          email: data?.email ?? current.email,
          role: data?.role ?? current.role,
        };

        localStorage.setItem("skillswap_user", JSON.stringify(updatedUser));

        return updatedUser;
      });
    } catch {
      // Keep the existing authentication state.
    }
  }, [token]);

  const login = useCallback(
    async (email, password) => {
      const { data } = await api.post("/auth/login", {
        email,
        password,
      });

      saveAuth(data);

      return data;
    },
    [saveAuth],
  );

  const register = useCallback(
    async (registerData) => {
      const { data } = await api.post("/auth/register", registerData);

      saveAuth(data);

      return data;
    },
    [saveAuth],
  );

  const logout = useCallback(() => {
    clearAuth();
  }, [clearAuth]);

  const updateName = useCallback((newName) => {
    setUser((current) => {
      if (!current) {
        return current;
      }

      const updatedUser = {
        ...current,
        name: newName,
      };

      localStorage.setItem("skillswap_user", JSON.stringify(updatedUser));

      return updatedUser;
    });
  }, []);

  useEffect(() => {
    const timer = setTimeout(() => {
      if (token) {
        refreshCredits();
      }

      setLoading(false);
    }, 0);

    return () => clearTimeout(timer);
  }, [token, refreshCredits]);

  const value = useMemo(
    () => ({
      token,
      user,
      credits,
      loading,
      isAuthenticated,
      login,
      register,
      logout,
      updateName,
      refreshCredits,
    }),
    [
      token,
      user,
      credits,
      loading,
      isAuthenticated,
      login,
      register,
      logout,
      updateName,
      refreshCredits,
    ],
  );

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}
