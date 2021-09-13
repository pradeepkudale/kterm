package com.pradale.kterm.domain.auth;

public interface Authentication {
    default String getUserName() {
        return null;
    }

    void setUserName(String username);
}
