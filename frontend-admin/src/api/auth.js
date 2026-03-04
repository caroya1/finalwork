import client from "./client";

export const login = (payload) =>
  client.post("/api/auth/login", payload).then((res) => res.data);

export const logout = (refreshToken, accessToken) =>
  client
    .post("/api/auth/logout", { refreshToken }, {
      headers: accessToken ? { Authorization: `Bearer ${accessToken}` } : undefined
    })
    .then((res) => res.data);
