package com.dianping.order.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class CreateOrderRequest {
    @NotNull(message = "userId is required")
    private Long userId;

    @NotNull(message = "shopId is required")
    private Long shopId;

    @NotNull(message = "amount is required")
    @Min(value = 1, message = "amount must be positive")
    private Integer amount;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getShopId() {
        return shopId;
    }

    public void setShopId(Long shopId) {
        this.shopId = shopId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
