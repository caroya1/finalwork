import client from "./client";

export const createShop = (payload) =>
  client.post("/api/shops", payload).then((res) => res.data);

export const listShops = (params) =>
  client.get("/api/shops", { params }).then((res) => res.data);

export const getShopDetail = (id) =>
  client.get(`/api/shops/${id}`).then((res) => res.data);

export const rateShop = (shopId, rating) =>
  client.post(`/api/shops/${shopId}/rate`, { rating }).then((res) => res.data);

export const listDishes = (shopId) =>
  client.get(`/api/shops/${shopId}/dishes`).then((res) => res.data);

export const addDish = (shopId, payload) =>
  client.post(`/api/shops/${shopId}/dishes`, payload).then((res) => res.data);
