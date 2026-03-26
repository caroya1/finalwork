package com.dianping.shop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dianping.shop.entity.Shop;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface ShopMapper extends BaseMapper<Shop> {
    @Select("SELECT * FROM dp_shop " +
            "WHERE audit_status = 1 " +
            "AND (#{city} IS NULL OR #{city} = '' OR city = #{city}) " +
            "AND (" +
            "name LIKE CONCAT('%', #{keyword}, '%') " +
            "OR category LIKE CONCAT('%', #{keyword}, '%') " +
            "OR tags LIKE CONCAT('%', #{keyword}, '%') " +
            "OR address LIKE CONCAT('%', #{keyword}, '%')" +
            ") " +
            "ORDER BY rating DESC, created_at DESC")
    List<Shop> search(@Param("city") String city, @Param("keyword") String keyword);
}
