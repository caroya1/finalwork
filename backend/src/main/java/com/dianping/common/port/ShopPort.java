package com.dianping.common.port;

import com.dianping.common.dto.ShopSummary;

import java.util.List;

public interface ShopPort {
    ShopSummary getSummary(Long id);

    List<ShopSummary> listSummaries(String city, String category);
}
