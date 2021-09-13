package com.pradale.kterm;

import javafx.application.Application;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;

@Slf4j
@SpringBootApplication
public class KTermLauncher {

    public static void main(String[] args) {
        Application.launch(KTermApplication.class, args);
    }

    @PreDestroy
    public void onExit() {
    }
}