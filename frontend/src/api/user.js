import client from "./client";

export const register = (payload) =>
  client.post("/api/users", payload).then((res) => res.data);
