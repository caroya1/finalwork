import client from "./client";

export const queryOrders = (payload) =>
  client.post("/api/admin/orders/query", payload).then((res) => res.data);

export const getTodayOrderCount = () =>
  client.get("/api/admin/orders/today-count").then((res) => res.data);

export const exportOrders = (params) =>
  client.get("/api/admin/orders/export", { params, responseType: "blob" });

export const listAdminMerchants = () =>
  client.get("/api/admin/merchants").then((res) => res.data);

export const approveMerchant = (merchantId) =>
  client.put(`/api/admin/merchants/${merchantId}/approve`).then((res) => res.data);

export const rejectMerchant = (merchantId) =>
  client.put(`/api/admin/merchants/${merchantId}/reject`).then((res) => res.data);

export const listAdminPosts = (auditStatus) =>
  client.get("/api/admin/posts", { params: { auditStatus } }).then((res) => res.data);

export const approvePost = (postId) =>
  client.put(`/api/admin/posts/${postId}/approve`).then((res) => res.data);

export const rejectPost = (postId, reason) =>
  client.put(`/api/admin/posts/${postId}/reject`, null, { params: { reason } }).then((res) => res.data);

export const listAdminShops = (auditStatus) =>
  client.get("/api/admin/shops", { params: { auditStatus } }).then((res) => res.data);

export const approveShop = (shopId) =>
  client.put(`/api/admin/shops/${shopId}/approve`).then((res) => res.data);

export const rejectShop = (shopId, reason) =>
  client.put(`/api/admin/shops/${shopId}/reject`, null, { params: { reason } }).then((res) => res.data);
