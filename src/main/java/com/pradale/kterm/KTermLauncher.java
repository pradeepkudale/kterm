package com.pradale.kterm;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.apache.sshd.client.SshClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@Slf4j
@SpringBootApplication
public class KTermLauncher {

    @Autowired
    private SshClient sshClient;

    public static void main(String[] args) {
        Application.launch(KTermApplication.class, args);
    }

    @PreDestroy
    public void onExit() {
        //sshClient.stop();
    }
}