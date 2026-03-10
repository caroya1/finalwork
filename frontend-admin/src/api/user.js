import client from "./client";

export const register = (payload) =>
  client.post("/api/users", payload).then((res) => res.data);

export const updateCity = (userId, city) =>
  client.put(`/api/users/${userId}/city`, { city }).then((res) => res.data);

export const listUsers = (status) =>
  client.get("/api/admin/users", { params: { status } }).then((res) => res.data);

export const updateUserStatus = (userId, status) =>
  client.put(`/api/admin/users/${userId}/status`, null, { params: { status } }).then((res) => res.data);
