package com.pradale.kterm.config;

import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import org.apache.sshd.client.SshClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.util.concurrent.Executors;

@Configuration
public class KtermConfiguration implements InitializingBean {

    @Value("${kterm.home}")
    private String home;

    @Value("${kterm.history}")
    private String history;

    @Value("${kterm.requests}")
    private String requests;

    @Bean
    public EventBus eventBus() {
        return new AsyncEventBus(Executors.newFixedThreadPool(10));
    }

    @Bean(destroyMethod = "stop")
    public SshClient getSshClient() {
        SshClient sshClient = SshClient.setUpDefaultClient();
        sshClient.start();
        return sshClient;
    }

    @Bean
    public XmlMapper xmlMapper() {
        XmlMapper xmlMapper = new XmlMapper();
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        return xmlMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        File dir = new File(home);
        if (!dir.exists()){
            dir.mkdirs();
        }

        dir = new File(history);
        if (!dir.exists()){
            dir.mkdirs();
        }

        dir = new File(requests);
        if (!dir.exists()){
            dir.mkdirs();
        }
    }
}
