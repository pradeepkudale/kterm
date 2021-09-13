package com.pradale.kterm.auth;

import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class NoAuthenticator extends AbstractAuthenticator implements HostAuthenticator {

    @Override
    public String getLabel() {
        return "No Authentication";
    }

    @Override
    public String getUserName() {
        return null;
    }

    @Override
    public String getPassword() {
        return null;
    }

    @Override
    public Resource getResource() {
        return null;
    }
}