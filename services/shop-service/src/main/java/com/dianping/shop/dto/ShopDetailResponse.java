package com.dianping.shop.dto;

import com.dianping.shop.entity.Shop;
import com.dianping.shop.entity.ShopDish;
import com.dianping.common.dto.PostSummary;

import java.util.List;

public class ShopDetailResponse {
    private Shop shop;
    private List<ShopDish> dishes;
    private List<PostSummary> posts;

    public ShopDetailResponse(Shop shop, List<ShopDish> dishes, List<PostSummary> posts) {
        this.shop = shop;
        this.dishes = dishes;
        this.posts = posts;
    }

    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }

    public List<ShopDish> getDishes() { return dishes; }
    public void setDishes(List<ShopDish> dishes) { this.dishes = dishes; }

    public List<PostSummary> getPosts() { return posts; }
    public void setPosts(List<PostSummary> posts) { this.posts = posts; }
}
