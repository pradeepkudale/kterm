package com.pradale.kterm.auth;

import com.pradale.kterm.domain.auth.BasicAuthentication;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

@Component
public class BasicAuthenticator extends AbstractAuthenticator<BasicAuthentication> implements HostAuthenticator, HTTPAuthenticator {

    private BasicAuthentication authentication;

    public void setAuthentication(BasicAuthentication authentication) {
        this.authentication = authentication;
    }

    @Override
    public String getLabel() {
        return "Basic Authentication";
    }

    @Override
    public String getUserName() {
        return authentication.getUserName();
    }

    @Override
    public String getPassword() {
        return authentication.getPassword();
    }

    @Override
    public Resource getResource() {
        return new ClassPathResource("/views/auth/host-basic-auth.fxml");
    }

    @Override
    public AuthTypes getAuthType() {
        return AuthTypes.BASIC;
    }
}
