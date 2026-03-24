import client from "./client";

export const merchantRegister = (payload) =>
  client.post("/api/merchants/register", payload).then((res) => res.data);

export const merchantLogin = (payload) =>
  client.post("/api/merchants/login", payload).then((res) => res.data);

export const createMerchant = (payload) =>
  client.post("/api/merchants", payload).then((res) => res.data);

export const listMerchants = () =>
  client.get("/api/merchants").then((res) => res.data);
