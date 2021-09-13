package com.pradale.kterm.domain.command;

import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.Request;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.auth.NoAuthentication;
import lombok.Builder;
import lombok.Data;

import java.util.HashSet;
import java.util.Set;

@Data
@Builder
public class ShellCommand {

    private String id;
    private Host host;
    private Request command;
    private String fetchProcessId = " & echo $!"; // To get the processId of executed command
    private Set<HostAuthentication> authentications;
    private HostAuthentication defaultAuthentication;
    private boolean isNew;

    public static ShellCommand getDefault(boolean isNew) {
        Set<HostAuthentication> auths = new HashSet<>(5);
        NoAuthentication defaultAuth = NoAuthentication.getDefault();

        ShellCommand dCommand = ShellCommand.builder()
                .command(Request.getDefault())
                .authentications(auths)
                .isNew(isNew)
                .build();
        auths.add(defaultAuth);
        dCommand.setDefaultAuthentication(defaultAuth);

        return dCommand;
    }

    public Host getHost() {
        if (host == null) {
            host = Host.builder().build();
        }
        return host;
    }

    public Request getCommand() {
        if (command == null) {
            command = Request.builder().build();
        }
        return command;
    }

    public <T extends HostAuthentication> T getHostAuthentication(Class<? extends HostAuthentication> classz) {
        return (T) authentications.stream()
                .filter(a -> a.getClass().equals(classz))
                .findFirst()
                .orElse(null);
    }
}
