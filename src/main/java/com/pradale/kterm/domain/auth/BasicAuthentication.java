package com.pradale.kterm.domain.auth;

import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
@JsonTypeName("BasicAuthentication")
public class BasicAuthentication implements HostAuthentication, HTTPAuthentication {

    private String userName;

    @EqualsAndHashCode.Exclude
    private String password;

    @Override
    public String getUserName() {
        return userName;
    }
}