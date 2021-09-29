package com.pradale.kterm.auth;

import org.springframework.core.io.Resource;

public interface Authenticator extends Comparable<Authenticator> {
    String getLabel();

    String getUserName();

    String getPassword();

    Resource getResource();

    default AuthTypes getAuthType() {
        return AuthTypes.NO_AUTH;
    }

    default int compareTo(Authenticator o) {
        return getAuthType().compareTo(o.getAuthType());
    }
}
