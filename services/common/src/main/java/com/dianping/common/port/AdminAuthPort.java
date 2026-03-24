package com.dianping.common.port;

import com.dianping.common.dto.AdminAuthView;

public interface AdminAuthPort {
    AdminAuthView findByUsername(String username);
}
