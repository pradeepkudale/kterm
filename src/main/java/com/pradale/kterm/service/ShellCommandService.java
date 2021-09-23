package com.pradale.kterm.service;

import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.type.ShellCommand;

public interface ShellCommandService {
    public void updateCommand(ShellCommand shellCommand, String value);

    public void updateHostName(ShellCommand shellCommand, String value);

    public void updateHostPort(ShellCommand shellCommand, Integer value);

    public void updateUserName(ShellCommand shellCommand, Class<? extends HostAuthentication> authType, String value);

    public void updatePassword(ShellCommand shellCommand, Class<? extends HostAuthentication> authType, String value);

    void updateDefaultAuthentication(ShellCommand shellCommand, Class<? extends HostAuthentication> authType);

    void save(ShellCommand shellCommand);
}
