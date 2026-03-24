package com.dianping.user.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.dianping.common.dto.AdminAuthView;
import com.dianping.common.port.AdminAuthPort;
import com.dianping.user.entity.Admin;
import com.dianping.user.mapper.AdminMapper;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service
@Primary
public class AdminAuthPortImpl implements AdminAuthPort {
    private final AdminMapper adminMapper;

    public AdminAuthPortImpl(AdminMapper adminMapper) {
        this.adminMapper = adminMapper;
    }

    @Override
    public AdminAuthView findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        Admin admin = adminMapper.selectOne(new LambdaQueryWrapper<Admin>()
                .eq(Admin::getUsername, username.trim()));
        if (admin == null) {
            return null;
        }
        return new AdminAuthView(
                admin.getId(),
                admin.getUsername(),
                admin.getPasswordHash(),
                admin.getStatus(),
                admin.getRole(),
                admin.getName()
        );
    }
}
