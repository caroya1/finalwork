package com.dianping.user.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.common.dto.OrderDTO;
import com.dianping.user.dto.BalanceRechargeRequest;
import com.dianping.user.dto.UserCreateRequest;
import com.dianping.user.dto.UpdateCityRequest;
import com.dianping.user.dto.UserProfileResponse;
import com.dianping.common.dto.PostSummary;
import com.dianping.common.dto.UserCouponView;
import com.dianping.common.port.PostPort;
import com.dianping.common.port.CouponPort;
import com.dianping.user.client.OrderClient;
import com.dianping.user.entity.User;
import com.dianping.user.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;
    private final PostPort postPort;
    private final CouponPort couponPort;
    private final OrderClient orderClient;
    private final Executor appTaskExecutor;

    public UserController(UserService userService, PostPort postPort,
                          CouponPort couponPort, OrderClient orderClient,
                          @Qualifier("appTaskExecutor") Executor appTaskExecutor) {
        this.userService = userService;
        this.postPort = postPort;
        this.couponPort = couponPort;
        this.orderClient = orderClient;
        this.appTaskExecutor = appTaskExecutor;
    }

    @PostMapping
    public ApiResponse<User> create(@Valid @RequestBody UserCreateRequest request) {
        return ApiResponse.ok(userService.create(request));
    }

    @GetMapping
    public ApiResponse<List<User>> list() {
        return ApiResponse.ok(userService.list());
    }

    @PutMapping("/{id}/city")
    public ApiResponse<User> updateCity(@PathVariable("id") Long id,
                                        @Valid @RequestBody UpdateCityRequest request) {
        return ApiResponse.ok(userService.updateCity(id, request.getCity()));
    }

    @PostMapping("/{id}/recharge")
    public ApiResponse<User> recharge(@PathVariable("id") Long id,
                                      @Valid @RequestBody BalanceRechargeRequest request) {
        return ApiResponse.ok(userService.recharge(id, BigDecimal.valueOf(request.getAmount())));
    }

    @GetMapping("/{id}/profile")
    public ApiResponse<UserProfileResponse> profile(@PathVariable("id") Long id) {
        User user = userService.findById(id);
        if (user == null) {
            return ApiResponse.fail("user not found");
        }
        // 同步调用，避免 SecurityContext 在异步线程中丢失
        List<PostSummary> posts = postPort.listSummariesByUser(id);
        List<UserCouponView> coupons = couponPort.listByUser(id);
        List<OrderDTO> orders = orderClient.listByUser(id);
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getUserRole(),
                user.getCity(),
                user.getBalance(),
                posts,
                coupons,
                orders
        );
        return ApiResponse.ok(response);
    }

    @PutMapping("/{id}/avatar")
    public ApiResponse<User> updateAvatar(@PathVariable("id") Long id,
                                          @RequestParam("avatarUrl") String avatarUrl) {
        return ApiResponse.ok(userService.updateAvatar(id, avatarUrl));
    }
}
