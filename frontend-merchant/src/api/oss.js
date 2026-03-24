import client from "./client";

export async function uploadImage(file, dir = "common") {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("dir", dir);
  const response = await client.post("/api/oss/upload/image", formData, {
    headers: { "Content-Type": "multipart/form-data" }
  });
  return response.data;
}
