package com.pradale.kterm.config;

import com.pradale.kterm.auth.AuthTypes;
import com.pradale.kterm.auth.Authenticator;
import com.pradale.kterm.auth.HostAuthenticator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
public class SecurityConfiguration {

    @Bean
    public PasswordEncoder encoder() {
        return new BCryptPasswordEncoder(16);
    }

    @Bean
    public Map<AuthTypes, Authenticator> hostAuthenticators(List<HostAuthenticator> hostAuthenticators) {
        Map<AuthTypes, Authenticator> map = new HashMap<>(5);

        for (HostAuthenticator authenticator: hostAuthenticators) {
            map.put(authenticator.getAuthType(), authenticator);
        }

        return map;
    }
}
