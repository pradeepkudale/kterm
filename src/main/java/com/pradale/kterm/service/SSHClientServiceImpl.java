package com.pradale.kterm.service;

import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.auth.BasicAuthentication;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.auth.NoAuthentication;
import com.pradale.kterm.domain.type.ShellCommand;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.apache.sshd.common.util.io.NoCloseInputStream;
import org.apache.sshd.common.util.io.NoCloseOutputStream;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

@Slf4j
@Service
public class SSHClientServiceImpl implements SSHClientService {

    @Override
    public String validate(String host, int port, String username, String password) {
        try {
            try (SshClient client = SshClient.setUpDefaultClient()) {
                client.start();

                try (ClientSession session = client.connect(username, host, port).verify().getSession()) {
                    if (StringUtils.isNotBlank(password)) {
                        session.addPasswordIdentity(password);
                    }
                    session.auth().verify();

                    try (ClientChannel channel = session.createExecChannel("ls -ltrl")) {
                        channel.setIn(new NoCloseInputStream(System.in));
                        channel.setOut(new NoCloseOutputStream(System.out));
                        channel.setErr(new NoCloseOutputStream(System.err));
                        channel.open();
                        channel.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), 0);
                    } finally {
                        session.close(true);
                    }
                } finally {
                    client.stop();
                }
            }
        } catch (Exception ex) {
            return ex.getMessage();
        }
        return null;
    }

    @Override
    public void execute(ShellCommand shellCommand) {
        HostAuthentication authentication = shellCommand.getDefaultAuthentication();

        if (authentication instanceof NoAuthentication) {
            NoAuthentication auth = (NoAuthentication) authentication;
            Host host = shellCommand.getHost();
            validate(host.getName(), host.getPort(), auth.getUserName(), null);
        } else if (authentication instanceof BasicAuthentication) {
            BasicAuthentication auth = (BasicAuthentication) authentication;
            Host host = shellCommand.getHost();
            validate(host.getName(), host.getPort(), auth.getUserName(), auth.getPassword());
        } else {
            throw new IllegalArgumentException(authentication.getClass() + " Type not supported");
        }
    }

    @Override
    public void execute(ShellCommand shellCommand, OutputStream outputStream) {
        try {
            try (SshClient client = SshClient.setUpDefaultClient()) {
                client.start();

                try (ClientSession session = client.connect("pradeep","127.0.0.1", 2222).verify().getSession()) {
                        session.addPasswordIdentity("Pradeep@1982");
                    session.auth().verify();

                    try (ClientChannel channel = session.createExecChannel("ls -ltrl")) {
                        channel.setOut(outputStream);
                        channel.setErr(outputStream);
                        channel.open();
                        channel.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), 0);
                    } finally {
                        session.close(true);
                    }
                } finally {
                    client.stop();
                }
            }
        } catch (Exception ex) {
            try {
                outputStream.write(ex.getMessage().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
