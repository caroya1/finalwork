package com.dianping.user.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class BalanceRechargeRequest {
    @NotNull(message = "amount is required")
    @Min(value = 1, message = "amount must be positive")
    private Integer amount;

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }
}
