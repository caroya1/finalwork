import axios from "axios";

const client = axios.create({
  baseURL: "http://localhost:8081",
  timeout: 10000
});

client.interceptors.request.use((config) => {
  const token = localStorage.getItem("dp_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const userId = localStorage.getItem("dp_user_id");
  if (userId) {
    config.headers["X-User-Id"] = userId;
  }
  const refreshToken = localStorage.getItem("dp_refresh_token");
  if (refreshToken) {
    config.headers["X-Refresh-Token"] = refreshToken;
  }
  return config;
});

client.interceptors.response.use((response) => {
  const nextAccess = response.headers?.authorization;
  const nextRefresh = response.headers?.["x-refresh-token"];
  if (nextAccess && nextAccess.startsWith("Bearer ")) {
    localStorage.setItem("dp_token", nextAccess.substring(7));
  }
  if (nextRefresh) {
    localStorage.setItem("dp_refresh_token", nextRefresh);
  }
  return response;
});

export default client;
