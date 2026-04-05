/**
 * 订单状态映射
 */
export const ORDER_STATUS = {
  0: '待支付',
  1: '已支付',
  2: '已核销',
  3: '已退款',
  4: '已取消'
};

/**
 * 审核状态映射
 */
export const AUDIT_STATUS = {
  0: '待审核',
  1: '已通过',
  2: '已拒绝'
};

/**
 * 获取订单状态文本
 * @param {number} status - 状态码
 * @returns {string} 状态文本
 */
export function getOrderStatusText(status) {
  return ORDER_STATUS[status] || '未知状态';
}

/**
 * 获取审核状态文本
 * @param {number} status - 状态码
 * @returns {string} 状态文本
 */
export function getAuditStatusText(status) {
  return AUDIT_STATUS[status] || '未知状态';
}
