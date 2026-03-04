import client from "./client";

export const createShop = (payload) =>
  client.post("/api/shops", payload).then((res) => res.data);

export const listShops = (params) =>
  client.get("/api/shops", { params }).then((res) => res.data);

export const createCoupon = (payload) =>
  client.post("/api/coupons", payload).then((res) => res.data);
