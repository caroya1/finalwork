import axios from "axios";

const client = axios.create({
  baseURL: "http://localhost:8081",
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
});

client.interceptors.request.use((config) => {
  // 调试：打印实际请求的URL
  console.log(`[API Request] ${config.method?.toUpperCase()} ${config.baseURL}${config.url}`);
  
  const token = localStorage.getItem("dp_token");
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  const refreshToken = localStorage.getItem("dp_refresh_token");
  if (refreshToken) {
    config.headers["X-Refresh-Token"] = refreshToken;
  }
  const merchantId = localStorage.getItem("dp_merchant_id");
  if (merchantId) {
    config.headers["X-Merchant-Id"] = merchantId;
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
    // 401 未授权 - 清除token并跳转登录页
    if (error.response?.status === 401) {
      localStorage.removeItem("dp_token");
      localStorage.removeItem("dp_refresh_token");
      localStorage.removeItem("dp_merchant_id");
      localStorage.removeItem("dp_merchant_name");
      localStorage.removeItem("dp_username");
      if (typeof window !== "undefined") {
        window.location.href = "/login";
      }
      return Promise.reject(error);
    }

    // 403 权限不足 - 也可能是token过期，清除token并重定向
    if (error.response?.status === 403) {
      // 如果是API调用返回403，清除token并跳转登录
      if (error.config?.url !== '/login') {
        localStorage.removeItem("dp_token");
        localStorage.removeItem("dp_refresh_token");
        localStorage.removeItem("dp_merchant_id");
        localStorage.removeItem("dp_merchant_name");
        localStorage.removeItem("dp_username");
        if (typeof window !== "undefined") {
          window.location.href = "/login";
        }
      }
      return Promise.reject({
        ...error,
        userMessage: "权限不足或登录已过期，请重新登录"
      });
    }

    // 500 服务器错误
    if (error.response?.status >= 500) {
      return Promise.reject({
        ...error,
        userMessage: "服务器开小差了，请稍后重试"
      });
    }

    // 网络错误
    if (!error.response) {
      return Promise.reject({
        ...error,
        userMessage: "网络连接失败，请检查网络设置"
      });
    }

    // 其他错误，提取服务器返回的错误信息
    const serverMessage = error.response?.data?.message;
    return Promise.reject({
      ...error,
      userMessage: serverMessage || "请求失败，请稍后重试"
    });
  }
);

export default client;
