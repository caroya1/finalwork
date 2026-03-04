import client from "./client";

export const createMerchant = (payload) =>
  client.post("/api/merchants", payload).then((res) => res.data);

export const listMerchants = () =>
  client.get("/api/merchants").then((res) => res.data);
