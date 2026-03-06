package com.dianping.common.dto;

import java.math.BigDecimal;

public class UserAuthView {
    private Long id;
    private String username;
    private String passwordHash;
    private String city;
    private String role;
    private BigDecimal balance;

    public UserAuthView() {
    }

    public UserAuthView(Long id, String username, String passwordHash, String city, String role, BigDecimal balance) {
        this.id = id;
        this.username = username;
        this.passwordHash = passwordHash;
        this.city = city;
        this.role = role;
        this.balance = balance;
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

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }
}
