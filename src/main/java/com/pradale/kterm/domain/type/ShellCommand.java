package com.pradale.kterm.domain.type;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;
import com.pradale.kterm.domain.Command;
import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.auth.NoAuthentication;
import lombok.Data;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

import java.util.HashSet;
import java.util.Set;

@Data
@SuperBuilder
@Jacksonized
@JacksonXmlRootElement(localName = "shellCommand")
public class ShellCommand extends TextItem implements Terminal {

    private String id;
    private Host host;
    private Command command;
    @JsonIgnore
    private String fetchProcessId = " & echo $!"; // To get the processId of executed command
    @JacksonXmlElementWrapper(localName = "authentications")
    @JacksonXmlProperty(localName = "authentication")
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    private Set<HostAuthentication> authentications;
    @JacksonXmlElementWrapper(localName = "defaultAuthentication")
    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
    private HostAuthentication defaultAuthentication;
    @JsonProperty(value = "isNew")
    private boolean isNew;

    public static ShellCommand getDefault(boolean isNew) {
        Set<HostAuthentication> auths = new HashSet<>(5);
        NoAuthentication defaultAuth = NoAuthentication.getDefault();

        ShellCommand dCommand = ShellCommand.builder()
                .host(Host.builder().build())
                .command(Command.getDefault())
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

    public Command getCommand() {
        if (command == null) {
            command = Command.builder().build();
        }
        return command;
    }

    public <T extends HostAuthentication> T getHostAuthentication(Class<? extends HostAuthentication> classz) {
        return (T) authentications.stream()
                .filter(a -> a.getClass().equals(classz))
                .findFirst()
                .orElse(null);
    }

    @Override
    public ItemType getType() {
        return ItemType.SHELL_COMMAND;
    }
}
