package com.pradale.kterm.service;

import com.google.common.eventbus.EventBus;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.type.ShellCommand;
import com.pradale.kterm.domain.type.Terminal;
import com.pradale.kterm.events.LoadTreeViewShellCommand;
import com.pradale.kterm.events.NotificationEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.List;

@Slf4j
@Service
public class InitializeServiceImpl extends AbstractService implements InitializeService {

    @Autowired
    private EventBus eventBus;

    @Override
    public void loadTabPane() {
        loadRequests();
    }

    private void loadRequests() {
        List<File> savedRequests = getSavedFiles();
        for (File file : savedRequests) {
            try {
                Terminal terminal = toTerminal(file);

                if (terminal instanceof ShellCommand) {
                    LoadTreeViewShellCommand event = new LoadTreeViewShellCommand();
                    ShellCommand command = (ShellCommand) terminal;
                    setDefaultAuth(command);
                    event.setShellCommand(command);
                    eventBus.post(event);
                }

            } catch (Exception ex) {
                log.error(ex.getMessage(), ex);
                eventBus.post(new NotificationEvent("Save Shell Request", ex.getMessage()));
            }
        }
    }

    private void setDefaultAuth(ShellCommand command) {
        HostAuthentication defaultAuth = command.getDefaultAuthentication();

        command.getAuthentications().stream().forEach(auth -> {
            if (auth.getClass().equals(defaultAuth.getClass())) {
                command.setDefaultAuthentication(auth);
            }
        });
    }
}