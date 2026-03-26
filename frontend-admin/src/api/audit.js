import client from "./client";

/**
 * 获取审核记录列表
 * 聚合帖子、评论、店铺、商户的审核数据
 */
export const getAuditRecords = async (params = {}) => {
  const { type, status, page = 1, pageSize = 10 } = params;
  
  try {
    // Fetch data from all sources in parallel
    const [postsRes, shopsRes, merchantsRes] = await Promise.all([
      client.get("/api/admin/posts", { params: { auditStatus: status } }).then(r => r.data),
      client.get("/api/admin/shops", { params: { auditStatus: status } }).then(r => r.data),
      client.get("/api/admin/merchants").then(r => r.data)
    ]);

    const posts = postsRes.success ? (postsRes.data || []) : [];
    const shops = shopsRes.success ? (shopsRes.data || []) : [];
    const merchants = merchantsRes.success ? (merchantsRes.data || []) : [];

    // Transform data into unified audit record format
    let records = [];

    // Add post records
    if (!type || type === 'post') {
      records.push(...posts.map(post => ({
        id: `post-${post.id}`,
        type: 'post',
        entityId: post.id,
        content: post.title || '无标题',
        status: post.auditStatus ?? 0,
        reason: post.auditRemark,
        auditTime: post.updatedAt,
        userId: post.userId,
        extra: { coverUrl: post.coverUrl, city: post.city }
      })));
    }

    // Add shop records
    if (!type || type === 'shop') {
      records.push(...shops.map(shop => ({
        id: `shop-${shop.id}`,
        type: 'shop',
        entityId: shop.id,
        content: shop.name || '未命名店铺',
        status: shop.auditStatus ?? 0,
        reason: shop.auditRemark,
        auditTime: shop.updatedAt,
        merchantId: shop.merchantId,
        extra: { category: shop.category, city: shop.city }
      })));
    }

    // Add merchant records (using aiAuditStatus, aiAuditReason, aiAuditTime)
    if (!type || type === 'merchant') {
      records.push(...merchants
        .filter(m => m.aiAuditStatus !== undefined && m.aiAuditStatus !== null)
        .map(merchant => ({
          id: `merchant-${merchant.id}`,
          type: 'merchant',
          entityId: merchant.id,
          content: merchant.name || '未命名商户',
          status: merchant.aiAuditStatus ?? 0,
          reason: merchant.aiAuditReason,
          auditTime: merchant.aiAuditTime,
          extra: { category: merchant.category, city: merchant.city }
        }))
      );
    }

    // Filter by status if specified
    if (status !== undefined && status !== null && status !== '') {
      records = records.filter(r => r.status === Number(status));
    }

    // Sort by audit time (descending)
    records.sort((a, b) => {
      const timeA = a.auditTime ? new Date(a.auditTime).getTime() : 0;
      const timeB = b.auditTime ? new Date(b.auditTime).getTime() : 0;
      return timeB - timeA;
    });

    // Pagination
    const total = records.length;
    const start = (page - 1) * pageSize;
    const end = start + pageSize;
    const paginatedRecords = records.slice(start, end);

    return {
      success: true,
      data: {
        records: paginatedRecords,
        total: total,
        page: page,
        pageSize: pageSize
      }
    };
  } catch (error) {
    console.error('Failed to fetch audit records:', error);
    return {
      success: false,
      message: '获取审核记录失败',
      data: {
        records: [],
        total: 0,
        page: page,
        pageSize: pageSize
      }
    };
  }
};

/**
 * 获取帖子列表（带审核状态）
 */
export const listAdminPosts = (auditStatus) =>
  client.get("/api/admin/posts", { params: { auditStatus } }).then((res) => res.data);

/**
 * 获取店铺列表（带审核状态）
 */
export const listAdminShops = (auditStatus) =>
  client.get("/api/admin/shops", { params: { auditStatus } }).then((res) => res.data);

/**
 * 获取商户列表
 */
export const listAdminMerchants = () =>
  client.get("/api/admin/merchants").then((res) => res.data);

/**
 * 审核通过帖子
 */
export const approvePost = (postId) =>
  client.put(`/api/admin/posts/${postId}/approve`).then((res) => res.data);

/**
 * 审核拒绝帖子
 */
export const rejectPost = (postId, reason) =>
  client.put(`/api/admin/posts/${postId}/reject`, null, { params: { reason } }).then((res) => res.data);

/**
 * 审核通过店铺
 */
export const approveShop = (shopId) =>
  client.put(`/api/admin/shops/${shopId}/approve`).then((res) => res.data);

/**
 * 审核拒绝店铺
 */
export const rejectShop = (shopId, reason) =>
  client.put(`/api/admin/shops/${shopId}/reject`, null, { params: { reason } }).then((res) => res.data);

/**
 * 审核通过商户
 */
export const approveMerchant = (merchantId) =>
  client.put(`/api/admin/merchants/${merchantId}/approve`).then((res) => res.data);

/**
 * 审核拒绝商户
 */
export const rejectMerchant = (merchantId) =>
  client.put(`/api/admin/merchants/${merchantId}/reject`).then((res) => res.data);