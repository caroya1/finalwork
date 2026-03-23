package com.dianping.common.context;

public class UserSession {
    private Long id;
    private String username;
    private String role;
    private String city;
    private Long merchantId; // 商户ID（仅商户角色有）

    public UserSession() {
    }

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

    public Long getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(Long merchantId) {
        this.merchantId = merchantId;
    }

    public boolean isAdmin() {
        return "ADMIN".equals(role) || "SUPER_ADMIN".equals(role);
    }

    public boolean isMerchant() {
        return "MERCHANT".equals(role);
    }

    public boolean isSuperAdmin() {
        return "SUPER_ADMIN".equals(role);
    }
}
