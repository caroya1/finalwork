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

client.interceptors.response.use(
  (response) => {
    const nextAccess = response.headers?.authorization;
    const nextRefresh = response.headers?.["x-refresh-token"];
    if (nextAccess && nextAccess.startsWith("Bearer ")) {
      localStorage.setItem("dp_token", nextAccess.substring(7));
    }
    if (nextRefresh) {
      localStorage.setItem("dp_refresh_token", nextRefresh);
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Token 过期或无效，清除登录状态并触发登录弹窗
      localStorage.removeItem("dp_token");
      localStorage.removeItem("dp_refresh_token");
      localStorage.removeItem("dp_user_id");
      localStorage.removeItem("dp_username");
      window.dispatchEvent(new CustomEvent("dp-auth-required"));
      window.dispatchEvent(new CustomEvent("dp-auth-change", { detail: { isLoggedIn: false } }));
    }
    return Promise.reject(error);
  }
);

export default client;
