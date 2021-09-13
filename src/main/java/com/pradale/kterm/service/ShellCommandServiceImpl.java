package com.pradale.kterm.service;

import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.Request;
import com.pradale.kterm.domain.auth.AuthenticationFactory;
import com.pradale.kterm.domain.auth.BasicAuthentication;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.auth.NoAuthentication;
import com.pradale.kterm.domain.command.ShellCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ShellCommandServiceImpl implements ShellCommandService {

    @Override
    public void updateCommand(ShellCommand shellCommand, String value) {
        if (StringUtils.isNotBlank(value)) {
            Request command = shellCommand.getCommand();
            command.setValue(value);
        }
    }

    @Override
    public void updateHostName(ShellCommand shellCommand, String value) {
        if (value != null) {
            Host host = shellCommand.getHost();
            host.setName(value);
        }
    }

    @Override
    public void updateHostPort(ShellCommand shellCommand, Integer value) {
        if (value != null) {
            Host host = shellCommand.getHost();
            host.setPort(value);
        }
    }

    @Override
    public void updateUserName(ShellCommand shellCommand, Class<? extends HostAuthentication> authType, String value) {
        getHostAuthentication(shellCommand, authType);
        for (HostAuthentication authentication : shellCommand.getAuthentications()) {
            authentication.setUserName(value);
        }
    }

    @Override
    public void updatePassword(ShellCommand shellCommand, Class<? extends HostAuthentication> authType, String value) {
        HostAuthentication hAuth = getHostAuthentication(shellCommand, authType);

        if (authType == NoAuthentication.class) {
            log.debug("Password update not required for NoAuthentication");
        } else if (authType == BasicAuthentication.class) {
            BasicAuthentication auth = (BasicAuthentication) hAuth;
            auth.setPassword(value);
        } else {
            throw new IllegalArgumentException(authType + " Type not supported");
        }
    }

    @Override
    public void updateDefaultAuthentication(ShellCommand shellCommand, Class<? extends HostAuthentication> authType) {
        HostAuthentication hAuth = getHostAuthentication(shellCommand, authType);
        shellCommand.setDefaultAuthentication(hAuth);
    }

    private HostAuthentication getHostAuthentication(ShellCommand shellCommand, Class<? extends HostAuthentication> authType) {
        HostAuthentication hAuth = shellCommand.getHostAuthentication(authType);

        if (hAuth == null) {
            hAuth = AuthenticationFactory.getInstance(authType);
            shellCommand.getAuthentications().add(hAuth);
        }

        return hAuth;
    }
}
