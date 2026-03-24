package com.dianping.common.port;

import com.dianping.common.dto.UserSummary;

public interface UserPort {
    UserSummary getSummary(Long userId);
}
