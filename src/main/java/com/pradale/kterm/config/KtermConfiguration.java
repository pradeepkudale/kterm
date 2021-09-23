package com.pradale.kterm.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
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

    @Value("${kterm.path.home}")
    private String homeDirectory;

    @Value("${kterm.path.history}")
    private String historyDirectory;

    @Value("${kterm.path.requests}")
    private String requestDirectory;

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
        JacksonXmlModule jacksonXmlModule = new JacksonXmlModule();
        XmlMapper xmlMapper = new XmlMapper(jacksonXmlModule);
        xmlMapper.enable(SerializationFeature.INDENT_OUTPUT);
        xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        return xmlMapper;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        File dir = new File(homeDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        dir = new File(historyDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        dir = new File(requestDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
}
