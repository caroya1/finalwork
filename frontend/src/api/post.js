import client from "./client";

export const listPosts = (params) =>
  client.get("/api/posts", { params }).then((res) => res.data);
