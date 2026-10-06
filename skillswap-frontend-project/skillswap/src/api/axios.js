import axios from "axios";

const api = axios.create({
  baseURL: "http://localhost:8080/api",
  headers: {
    "Content-Type": "application/json",
  },
});

api.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem("skillswap_token");

    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => {
    return Promise.reject(error);
  },
);

api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem("skillswap_token");
      localStorage.removeItem("skillswap_user");
    }

    return Promise.reject(error);
  },
);

export function getErrorMessage(error) {
  const data = error?.response?.data;

  if (!data) {
    return error?.message || "Something went wrong. Please try again.";
  }

  if (typeof data === "string") {
    return data;
  }

  if (data.message) {
    return data.message;
  }

  if (data.error) {
    return data.error;
  }

  if (data.errors) {
    if (Array.isArray(data.errors)) {
      return data.errors.join(", ");
    }

    if (typeof data.errors === "object") {
      return Object.values(data.errors).flat().join(", ");
    }
  }

  return error?.message || "Something went wrong. Please try again.";
}

export default api;
