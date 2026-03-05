package com.dianping.user.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.user.dto.BalanceRechargeRequest;
import com.dianping.user.dto.UserCreateRequest;
import com.dianping.user.dto.UpdateCityRequest;
import com.dianping.user.dto.UserProfileResponse;
import com.dianping.post.entity.Post;
import com.dianping.post.service.PostService;
import com.dianping.coupon.dto.UserCouponView;
import com.dianping.coupon.service.UserCouponService;
import com.dianping.user.entity.User;
import com.dianping.user.service.UserService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/users")
@Validated
public class UserController {
    private final UserService userService;
    private final PostService postService;
    private final UserCouponService userCouponService;
    private final Executor appTaskExecutor;

    public UserController(UserService userService, PostService postService,
                          UserCouponService userCouponService,
                          @Qualifier("appTaskExecutor") Executor appTaskExecutor) {
        this.userService = userService;
        this.postService = postService;
        this.userCouponService = userCouponService;
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
        CompletableFuture<List<Post>> postsFuture = CompletableFuture.supplyAsync(
                () -> postService.listByUser(id), appTaskExecutor);
        CompletableFuture<List<UserCouponView>> couponsFuture = CompletableFuture.supplyAsync(
                () -> userCouponService.listByUser(id), appTaskExecutor);
        List<Post> posts = postsFuture.join();
        List<UserCouponView> coupons = couponsFuture.join();
        UserProfileResponse response = new UserProfileResponse(
                user.getId(),
                user.getUsername(),
                user.getUserRole(),
                user.getCity(),
                user.getBalance(),
                posts,
                coupons
        );
        return ApiResponse.ok(response);
    }
}
