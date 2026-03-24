package com.dianping.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dianping.order.entity.Order;
import com.dianping.order.dto.OrderQueryRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDateTime;
import java.util.List;

@Mapper
public interface OrderMapper extends BaseMapper<Order> {

    @Select("<script>" +
            "SELECT * FROM dp_order WHERE 1=1 " +
            "<if test='req.userId != null'> AND user_id = #{req.userId}</if>" +
            "<if test='req.shopId != null'> AND shop_id = #{req.shopId}</if>" +
            "<if test='req.status != null'> AND status = #{req.status}</if>" +
            "<if test='req.orderNo != null and req.orderNo != \"\"'> AND order_no LIKE CONCAT('%', #{req.orderNo}, '%')</if>" +
            "<if test='req.startTime != null'> AND created_at >= #{req.startTime}</if>" +
            "<if test='req.endTime != null'> AND created_at &lt;= #{req.endTime}</if>" +
            " ORDER BY created_at DESC" +
            "</script>")
    IPage<Order> queryPage(Page<Order> page, @Param("req") OrderQueryRequest req);

    @Select("SELECT * FROM dp_order WHERE status = #{status} AND created_at < #{expireTime}")
    List<Order> findExpiredOrders(@Param("status") Integer status, @Param("expireTime") LocalDateTime expireTime);

    @Select("SELECT COUNT(*) FROM dp_order WHERE shop_id = #{shopId} AND status IN (1, 2)")
    Integer countPaidOrdersByShop(@Param("shopId") Long shopId);

    @Select("SELECT COALESCE(SUM(pay_amount), 0) FROM dp_order WHERE shop_id = #{shopId} AND status IN (1, 2)")
    Integer sumRevenueByShop(@Param("shopId") Long shopId);

    @Select("SELECT COUNT(*) FROM dp_order WHERE DATE(created_at) = CURDATE()")
    Integer countTodayOrders();
}
