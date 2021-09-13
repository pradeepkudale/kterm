package com.pradale.kterm.service;

import com.pradale.kterm.domain.command.ShellCommand;

import java.io.OutputStream;

public interface SSHClientService {
    String validate(String host, int port, String username, String password);

    void execute(ShellCommand shellCommand);

    void execute(ShellCommand shellCommand, OutputStream outputStream);
}
