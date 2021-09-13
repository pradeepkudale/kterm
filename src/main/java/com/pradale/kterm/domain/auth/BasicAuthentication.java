package com.pradale.kterm.domain.auth;

import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@Builder
public class BasicAuthentication implements HostAuthentication, HTTPAuthentication {

    private String userName;

    @EqualsAndHashCode.Exclude
    private String password;

    @Override
    public String getUserName() {
        return userName;
    }
}