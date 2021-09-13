package com.pradale.kterm.config;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.apache.sshd.client.SshClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;

@Configuration
public class KtermConfiguration {

    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus(Executors.newFixedThreadPool(10));
    }

    @Bean
    public SshClient getSshClient() {
        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        return sshClient;
    }
}
