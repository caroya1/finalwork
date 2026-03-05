import client from "./client";

export const listPosts = (params) =>
  client.get("/api/posts", { params }).then((res) => res.data);

export const getPostDetail = (id) =>
  client.get(`/api/posts/${id}`).then((res) => res.data);

export const likePost = (id) =>
  client.post(`/api/posts/${id}/like`).then((res) => res.data);

export const unlikePost = (id) =>
  client.delete(`/api/posts/${id}/like`).then((res) => res.data);

export const addComment = (id, content) =>
  client.post(`/api/posts/${id}/comments`, { content }).then((res) => res.data);

export const createPost = (payload) =>
  client.post("/api/posts", payload).then((res) => res.data);
