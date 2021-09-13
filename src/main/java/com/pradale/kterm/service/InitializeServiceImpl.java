package com.pradale.kterm.service;

import com.google.common.eventbus.EventBus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InitializeServiceImpl implements InitializeService {

    @Autowired
    private EventBus eventBus;

    @Override
    public void loadTabPane() {
        loadRequests();
    }

    private void loadRequests() {
        //eventBus.post(new CommandLineTabRequestEvent());
    }
}
