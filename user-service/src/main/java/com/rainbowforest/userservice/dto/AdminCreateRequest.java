package com.rainbowforest.userservice.dto;

import com.rainbowforest.userservice.entity.User;

public class AdminCreateRequest {
    private String adminUserName;
    private String adminPassword;
    private User user;

    public String getAdminUserName() {
        return adminUserName;
    }

    public void setAdminUserName(String adminUserName) {
        this.adminUserName = adminUserName;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
