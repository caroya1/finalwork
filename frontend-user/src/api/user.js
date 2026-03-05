import client from "./client";

export const register = (payload) =>
  client.post("/api/users", payload).then((res) => res.data);

export const updateCity = (userId, city) =>
  client.put(`/api/users/${userId}/city`, { city }).then((res) => res.data);

export const rechargeBalance = (userId, amount) =>
  client.post(`/api/users/${userId}/recharge`, { amount }).then((res) => res.data);

export const getProfile = (userId) =>
  client.get(`/api/users/${userId}/profile`).then((res) => res.data);
