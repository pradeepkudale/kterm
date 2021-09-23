package com.pradale.kterm.domain.auth;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonTypeName("NoAuthentication")
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
