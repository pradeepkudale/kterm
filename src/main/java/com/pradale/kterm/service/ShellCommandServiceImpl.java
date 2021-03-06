package com.pradale.kterm.service;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.eventbus.EventBus;
import com.pradale.kterm.domain.Command;
import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.auth.AuthenticationFactory;
import com.pradale.kterm.domain.auth.BasicAuthentication;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.auth.NoAuthentication;
import com.pradale.kterm.domain.type.ShellCommand;
import com.pradale.kterm.events.AlertEvent;
import com.pradale.kterm.events.LoadShellCommandEvent;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;

@Slf4j
@Service
public class ShellCommandServiceImpl extends AbstractService implements ShellCommandService {

    @Value("${kterm.path.history}")
    private String historyDirectory;

    @Value("${kterm.path.requests}")
    private String requestDirectory;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private XmlMapper xmlMapper;

    @Override
    public void updateCommand(ShellCommand shellCommand, String value) {
        if (StringUtils.isNotBlank(value)) {
            Command command = shellCommand.getCommand();
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

    @Override
    public void save(ShellCommand shellCommand) {
        try {
            String filePath = requestDirectory + File.separator + shellCommand.getId() + ".xml";
            if(StringUtils.isNotBlank(shellCommand.getFilePath())) {
                filePath = shellCommand.getFilePath();
            }
            shellCommand.setFilePath(filePath);
            save(filePath, shellCommand);
            eventBus.post(LoadShellCommandEvent.builder().shellCommand(shellCommand).build());
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            eventBus.post(new AlertEvent("Shell Command", ex.getMessage()));
        }
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
