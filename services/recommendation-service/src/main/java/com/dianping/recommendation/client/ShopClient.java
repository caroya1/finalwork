package com.dianping.recommendation.client;

import com.dianping.common.dto.ShopSummary;
import com.dianping.common.port.ShopPort;
import com.dianping.common.dto.ShopDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "shop-service")
public interface ShopClient extends ShopPort {
    @Override
    @GetMapping("/internal/shops/{id}/summary")
    ShopSummary getSummary(@PathVariable("id") Long id);

    @Override
    @GetMapping("/internal/shops/summaries")
    List<ShopSummary> listSummaries(@RequestParam(value = "city", required = false) String city,
                                    @RequestParam(value = "category", required = false) String category);
    
    /**
     * 根据ID获取店铺详情
     */
    @GetMapping("/internal/shops/{id}")
    ShopDTO getById(@PathVariable("id") Long id);
    
    /**
     * 根据城市获取店铺列表
     */
    @GetMapping("/internal/shops/listByCity")
    List<ShopDTO> listByCity(@RequestParam("city") String city);
}
