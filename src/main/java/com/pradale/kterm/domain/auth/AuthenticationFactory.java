package com.pradale.kterm.domain.auth;

public class AuthenticationFactory {

    public static <T extends Authentication> T getInstance(Class<? extends Authentication> classz) {

        if(classz == NoAuthentication.class) {
            return (T) NoAuthentication.builder().build();
        }else if(classz == BasicAuthentication.class) {
            return (T) BasicAuthentication.builder().build();
        }

        throw new IllegalArgumentException(classz + " Type not supported");
    }
}
