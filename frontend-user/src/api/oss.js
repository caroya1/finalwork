import client from "./client";

/**
 * 上传图片
 * @param {File} file - 图片文件
 * @param {string} dir - 目录：posts, shops, users, dishes
 * @returns {Promise<{success: boolean, data: {url: string, filename: string, size: number}, message?: string}>}
 */
export async function uploadImage(file, dir = "common") {
  const formData = new FormData();
  formData.append("file", file);
  formData.append("dir", dir);

  const response = await client.post("/api/oss/upload/image", formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
  // 后端返回格式: { success: true, data: { url, filename, size, contentType }, message: "ok" }
  return response.data;
}

/**
 * 批量上传图片
 * @param {File[]} files - 图片文件数组
 * @param {string} dir - 目录
 * @returns {Promise<{success: boolean, data: Array<{url: string, filename: string, size: number}>, message?: string}>}
 */
export async function uploadImages(files, dir = "common") {
  const formData = new FormData();
  files.forEach(file => formData.append("files", file));
  formData.append("dir", dir);

  const response = await client.post("/api/oss/upload/images", formData, {
    headers: {
      "Content-Type": "multipart/form-data"
    }
  });
  return response.data;
}

/**
 * 删除文件
 * @param {string} fileUrl - 文件URL
 * @returns {Promise<{success: boolean, data: null, message?: string}>}
 */
export async function deleteFile(fileUrl) {
  const response = await client.post("/api/oss/delete", null, {
    params: { fileUrl }
  });
  return response.data;
}
