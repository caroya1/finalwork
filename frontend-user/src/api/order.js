import client from "./client";

/**
 * 创建订单（到店消费）
 * @param {Object} data - { shopId, amount, couponId?, remark? }
 * @returns {Promise<{success: boolean, data?: Object, message?: string}>}
 */
export const createOrder = (data) => {
  const userId = localStorage.getItem("dp_user_id");
  return client.post("/api/orders", {
    userId: Number(userId),
    ...data
  }).then(res => res.data);
};

/**
 * 获取订单详情
 * @param {number} orderId 
 * @returns {Promise<{success: boolean, data?: Object, message?: string}>}
 */
export const getOrder = (orderId) => {
  return client.get(`/api/orders/${orderId}`).then(res => res.data);
};

/**
 * 获取用户订单列表
 * @returns {Promise<{success: boolean, data?: Object[], message?: string}>}
 */
export const listUserOrders = () => {
  const userId = localStorage.getItem("dp_user_id");
  return client.get(`/api/orders/user/${userId}`).then(res => res.data);
};

/**
 * 取消订单
 * @param {number} orderId 
 * @param {string} reason 
 * @returns {Promise<{success: boolean, data?: Object, message?: string}>}
 */
export const cancelOrder = (orderId, reason) => {
  return client.post(`/api/orders/${orderId}/cancel`, { reason }).then(res => res.data);
};

/**
 * 支付订单
 * @param {number} orderId
 * @param {number} payAmount
 * @returns {Promise<{success: boolean, data?: Object, message?: string}>}
 */
export const payOrder = (orderId, payAmount) => {
  return client.post(`/api/orders/${orderId}/pay?payAmount=${payAmount}`).then(res => res.data);
};

export default { createOrder, getOrder, listUserOrders, payOrder, cancelOrder };
