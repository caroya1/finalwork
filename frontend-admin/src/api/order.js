import client from "./client";

export const listOrders = (userId) =>
  client.get("/api/orders", { params: { userId } }).then((res) => res.data);
