import client from "./client";

export const register = (payload) =>
  client.post("/api/users", payload).then((res) => res.data);

export const updateCity = (userId, city) =>
  client.put(`/api/users/${userId}/city`, { city }).then((res) => res.data);
