package com.pradale.kterm.service.ssh;

import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.auth.BasicAuthentication;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.auth.NoAuthentication;
import com.pradale.kterm.domain.command.ShellCommand;
import javafx.application.Platform;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.sshd.client.SshClient;
import org.apache.sshd.client.channel.ClientChannel;
import org.apache.sshd.client.channel.ClientChannelEvent;
import org.apache.sshd.client.session.ClientSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;

@Slf4j
@Component
public class SSHClientProcess {

    @Autowired
    private SshClient sshClient;

    public void execute(ShellCommand shellCommand, OutputStream outputStream) {
        try {
            try (ClientSession session = getClientSession(shellCommand)) {
                try (ClientChannel channel = session.createExecChannel(shellCommand.getCommand().getValue())) {
                    channel.open();
                    Platform.runLater(new Runnable() {
                        @Override
                        public void run() {
                            channel.setOut(outputStream);
                            channel.setErr(outputStream);
                        }
                    });
                    channel.waitFor(Arrays.asList(ClientChannelEvent.CLOSED), 0);
                } finally {
                    session.close(true);
                }
            }
        } catch (Exception ex) {
            try {
                log.error(ex.getMessage(), ex);
                outputStream.write(ex.getMessage().getBytes());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private ClientSession getClientSession(ShellCommand shellCommand) throws IOException {
        Host host = shellCommand.getHost();
        HostAuthentication authentication = shellCommand.getDefaultAuthentication();
        String username = authentication.getUserName();
        String password = "Pradeep@1982";//getIdentity(authentication);

        ClientSession session = sshClient.connect(username, host.getName(), host.getPort()).verify().getSession();
        if (StringUtils.isNotBlank(password)) {
            session.addPasswordIdentity(password);
        }
        session.auth().verify();

        return session;
    }

    private String getIdentity(HostAuthentication authentication) {
        if (authentication instanceof NoAuthentication) {
            return null;
        } else if (authentication instanceof BasicAuthentication) {
            return ((BasicAuthentication) authentication).getPassword();
        }

        return null;
    }
}
