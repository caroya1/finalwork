import client from "./client";

export const login = (payload) =>
  client.post("/api/auth/login", payload).then((res) => res.data);
