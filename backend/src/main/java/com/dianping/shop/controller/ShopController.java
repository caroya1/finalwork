package com.dianping.shop.controller;

import com.dianping.common.api.ApiResponse;
import com.dianping.shop.dto.ShopDetailResponse;
import com.dianping.shop.dto.ShopDishRequest;
import com.dianping.shop.dto.ShopRatingRequest;
import com.dianping.shop.entity.Shop;
import com.dianping.shop.entity.ShopDish;
import com.dianping.shop.service.ShopService;
import com.dianping.shop.service.ShopRatingService;
import com.dianping.shop.service.ShopDishService;
import com.dianping.post.entity.Post;
import com.dianping.post.service.PostService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@RestController
@RequestMapping("/api/shops")
@Validated
public class ShopController {
    private final ShopService shopService;
    private final ShopRatingService shopRatingService;
    private final ShopDishService shopDishService;
    private final PostService postService;
    private final Executor appTaskExecutor;

    public ShopController(ShopService shopService, ShopRatingService shopRatingService,
                          ShopDishService shopDishService, PostService postService,
                          @Qualifier("appTaskExecutor") Executor appTaskExecutor) {
        this.shopService = shopService;
        this.shopRatingService = shopRatingService;
        this.shopDishService = shopDishService;
        this.postService = postService;
        this.appTaskExecutor = appTaskExecutor;
    }

    @PostMapping
    public ApiResponse<Shop> create(@Valid @RequestBody Shop shop) {
        return ApiResponse.ok(shopService.create(shop));
    }

    @GetMapping
    public ApiResponse<List<Shop>> list(@RequestParam(value = "city", required = false) String city,
                                        @RequestParam(value = "category", required = false) String category,
                                        @RequestParam(value = "keyword", required = false) String keyword) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return ApiResponse.ok(shopService.search(city, keyword));
        }
        return ApiResponse.ok(shopService.list(city, category));
    }

    @GetMapping("/{id}")
    public ApiResponse<ShopDetailResponse> detail(@PathVariable("id") Long id) {
        Shop shop = shopService.getById(id);
        if (shop == null) {
            return ApiResponse.fail("shop not found");
        }
        CompletableFuture<List<ShopDish>> dishesFuture = CompletableFuture.supplyAsync(
                () -> shopDishService.listByShopId(id), appTaskExecutor);
        CompletableFuture<List<Post>> postsFuture = CompletableFuture.supplyAsync(
                () -> postService.list(null, null, id), appTaskExecutor);
        List<ShopDish> dishes = dishesFuture.join();
        List<Post> posts = postsFuture.join();
        return ApiResponse.ok(new ShopDetailResponse(shop, dishes, posts));
    }

    @PostMapping("/{id}/rate")
    public ApiResponse<Void> rate(@PathVariable("id") Long id,
                                  @RequestBody ShopRatingRequest request,
                                  Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ApiResponse.fail("login required");
        }
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        shopRatingService.rate(id, userId, request);
        return ApiResponse.ok(null);
    }

    @GetMapping("/{id}/dishes")
    public ApiResponse<List<ShopDish>> listDishes(@PathVariable("id") Long id) {
        return ApiResponse.ok(shopDishService.listByShopId(id));
    }

    @PostMapping("/{id}/dishes")
    public ApiResponse<ShopDish> addDish(@PathVariable("id") Long id,
                                         @RequestBody ShopDishRequest request,
                                         Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return ApiResponse.fail("login required");
        }
        Long userId = Long.parseLong(authentication.getPrincipal().toString());
        ShopDish dish = new ShopDish();
        dish.setName(request.getName());
        dish.setPrice(request.getPrice());
        dish.setDescription(request.getDescription());
        return ApiResponse.ok(shopDishService.addDish(id, userId, dish));
    }
}
