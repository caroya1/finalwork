package com.dianping.common.context;

public class UserSession {
    private Long id;
    private String username;
    private String role;
    private String city;

    public UserSession(Long id, String username, String role, String city) {
        this.id = id;
        this.username = username;
        this.role = role;
        this.city = city;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }
}
