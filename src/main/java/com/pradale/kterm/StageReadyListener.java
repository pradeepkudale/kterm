package com.pradale.kterm;

import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.input.KeyEvent;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationListener;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URL;

@Slf4j
@Component
public class StageReadyListener implements ApplicationListener<StageReadyEvent> {

    private final String applicationTitle;
    private final Resource fxml;
    private final ApplicationContext applicationContext;

    public StageReadyListener(@Value("${spring.application.name}") String applicationTitle,
                              @Value("classpath:/views/kterm-parent.fxml") Resource fxml,
                              ApplicationContext applicationContext) {
        this.applicationTitle = applicationTitle;
        this.fxml = fxml;
        this.applicationContext = applicationContext;
    }

    @Override
    public void onApplicationEvent(StageReadyEvent event) {
        try {
            Stage stage = event.getStage();
            URL url = fxml.getURL();
            FXMLLoader fxmlLoader = new FXMLLoader(url);
            fxmlLoader.setControllerFactory(applicationContext::getBean);
            Parent root = fxmlLoader.load();
            Scene scene = new Scene(root, 1100, 600);
            stage.setScene(scene);
            stage.setTitle(this.applicationTitle);
            scene.getStylesheets().add("kterm.css");
            stage.show();

            scene.setOnKeyPressed(new EventHandler<KeyEvent>() {
                @Override
                public void handle(KeyEvent event) {
                    // TODO : add global
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}