package com.pradale.kterm.domain.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NoAuthentication implements HostAuthentication, HTTPAuthentication {
    private String userName;

    public static NoAuthentication getDefault() {
        return NoAuthentication.builder().build();
    }

    @Override
    public String getUserName() {
        return userName;
    }
}
