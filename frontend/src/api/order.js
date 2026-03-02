import client from "./client";

export const createOrder = (payload) =>
  client.post("/api/orders", payload).then((res) => res.data);

export const listOrders = (userId) =>
  client.get("/api/orders", { params: { userId } }).then((res) => res.data);
