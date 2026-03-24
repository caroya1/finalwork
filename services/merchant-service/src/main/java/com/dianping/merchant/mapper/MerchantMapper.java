package com.dianping.merchant.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.dianping.merchant.entity.Merchant;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface MerchantMapper extends BaseMapper<Merchant> {

    @Select("SELECT * FROM dp_merchant WHERE email = #{email}")
    Merchant findByEmail(String email);

    @Select("SELECT COUNT(*) FROM dp_merchant WHERE status = #{status}")
    Integer countByStatus(Integer status);
}
