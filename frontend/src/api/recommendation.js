import client from "./client";

export const fetchRecommendations = (payload) =>
  client.post("/api/recommendations", payload).then((res) => res.data);
