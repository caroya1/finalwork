import client from "./client";

export const createShop = (payload) =>
  client.post("/api/merchant/shops", payload).then((res) => res.data);

export const listMyShops = () =>
  client.get("/api/merchant/shops").then((res) => res.data);

export const updateShop = (shopId, payload) =>
  client.put(`/api/merchant/shops/${shopId}`, payload).then((res) => res.data);

export const deleteShop = (shopId) =>
  client.delete(`/api/merchant/shops/${shopId}`).then((res) => res.data);

export const listDishes = (shopId) =>
  client.get(`/api/merchant/shops/${shopId}/dishes`).then((res) => res.data);

export const createDish = (shopId, payload) =>
  client.post(`/api/merchant/shops/${shopId}/dishes`, payload).then((res) => res.data);

export const updateDish = (shopId, dishId, payload) =>
  client.put(`/api/merchant/shops/${shopId}/dishes/${dishId}`, payload).then((res) => res.data);

export const deleteDish = (shopId, dishId) =>
  client.delete(`/api/merchant/shops/${shopId}/dishes/${dishId}`).then((res) => res.data);

export const createCoupon = (payload) =>
  client.post("/api/merchant/coupons", payload).then((res) => res.data);

export const listCouponsByShop = (shopId) =>
  client.get(`/api/merchant/coupons/shop/${shopId}`).then((res) => res.data);

export const listOrdersByShop = (shopId, status) =>
  client.get(`/api/orders/shop/${shopId}`, { params: { status } }).then((res) => res.data);

export const verifyOrder = (orderId) =>
  client.post(`/api/orders/${orderId}/verify`).then((res) => res.data);

export const getOrderStats = (shopId) =>
  client.get(`/api/orders/shop/${shopId}/stats`).then((res) => res.data);
