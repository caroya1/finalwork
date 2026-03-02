import client from "./client";

export const createShop = (payload) =>
  client.post("/api/shops", payload).then((res) => res.data);

export const listShops = (city) =>
  client.get("/api/shops", { params: { city } }).then((res) => res.data);
