package com.dianping.ai.strategy;

import com.dianping.common.dto.ShopDTO;

import java.util.List;

/**
 * 推荐策略接口
 * AI服务本地接口，与recommendation-service接口保持一致
 */
public interface RecommendStrategy {

    /**
     * 策略名称
     */
    String getName();

    /**
     * 执行推荐
     *
     * @param userId    用户ID
     * @param city      城市
     * @param longitude 经度（可选）
     * @param latitude  纬度（可选）
     * @param size      推荐数量
     * @return 推荐店铺列表
     */
    List<ShopDTO> recommend(Long userId, String city, Double longitude, Double latitude, Integer size);

    /**
     * 是否支持该用户（用于判断冷启动）
     *
     * @param userId 用户ID
     * @return true-支持，false-不支持
     */
    boolean isSupported(Long userId);
}
