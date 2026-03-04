package com.dianping.shop.dto;

import com.dianping.shop.entity.Shop;
import com.dianping.shop.entity.ShopDish;
import com.dianping.post.entity.Post;

import java.util.List;

public class ShopDetailResponse {
    private Shop shop;
    private List<ShopDish> dishes;
    private List<Post> posts;

    public ShopDetailResponse(Shop shop, List<ShopDish> dishes, List<Post> posts) {
        this.shop = shop;
        this.dishes = dishes;
        this.posts = posts;
    }

    public Shop getShop() { return shop; }
    public void setShop(Shop shop) { this.shop = shop; }

    public List<ShopDish> getDishes() { return dishes; }
    public void setDishes(List<ShopDish> dishes) { this.dishes = dishes; }

    public List<Post> getPosts() { return posts; }
    public void setPosts(List<Post> posts) { this.posts = posts; }
}
