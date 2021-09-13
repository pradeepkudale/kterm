package com.pradale.kterm.auth;

import org.springframework.core.io.Resource;

public interface Authenticator {
    String getLabel();

    String getUserName();

    String getPassword();

    Resource getResource();

    default AuthTypes getAuthType() {
        return AuthTypes.NO_AUTH;
    }
}
