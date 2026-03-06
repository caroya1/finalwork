package com.dianping.common.dto;

public class ShopSummary {
    private Long id;
    private String name;
    private String category;
    private String city;
    private Double rating;
    private String address;

    public ShopSummary() {
    }

    public ShopSummary(Long id, String name, String category, String city, Double rating, String address) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.city = city;
        this.rating = rating;
        this.address = address;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
