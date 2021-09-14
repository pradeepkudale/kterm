package com.pradale.kterm.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pradale.kterm.domain.command.ShellCommand;
import com.pradale.kterm.events.AlertEvent;
import com.pradale.kterm.events.NotificationEvent;
import com.pradale.kterm.handler.ShellCommandEventHandler;
import com.pradale.kterm.service.InitializeService;
import com.pradale.kterm.service.SSHClientService;
import com.pradale.kterm.utils.ApplicationUtils;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Duration;
import lombok.SneakyThrows;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;

import java.net.URL;

@Controller
public class KTermController {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InitializeService initializeService;

    @Autowired
    private SSHClientService sshClientService;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private ShellCommandEventHandler commandRequestEventHandler;

    @Value("classpath:/views/requests/shell-command.fxml")
    private Resource commandRequestFxml;

    @FXML
    private TabPane ctrlTabPanelParent;

    @FXML
    private BorderPane ktermParentPane;

    private ContextMenu tabContextMenu;

    @FXML
    public void initialize() {
        eventBus.register(this);
        initializeService.loadTabPane();
    }

    @Subscribe
    public void loadCommandTab(ShellCommand shellCommand) {
        BorderPane tabPanel = loadCommandLineFxml();
        Tab tab = new Tab();

        tab.setClosable(true);
        tab.setContent(tabPanel);
        tab.setContextMenu(getTabContextMenu());

        if (shellCommand.isNew()) {
            String id = ApplicationUtils.randomId("Shell");
            tab.setText(id);
            tab.setId(id);
            shellCommand.setId(id);
        } else {
            tab.setText(shellCommand.getId());
            tab.setId(shellCommand.getId());
        }

        commandRequestEventHandler.initialize(tab, tabPanel, shellCommand);
        ctrlTabPanelParent.getTabs().add(tab);
    }

    public void addCommandRequest(ActionEvent actionEvent) {
        loadCommandTab(ShellCommand.getDefault(true));
    }

    @SneakyThrows
    private BorderPane loadCommandLineFxml() {
        URL url = commandRequestFxml.getURL();

        BorderPane commandLineFxml = FXMLLoader.load(url);
        commandLineFxml.setMaxHeight(Double.MAX_VALUE);
        commandLineFxml.setMaxWidth(Double.MAX_VALUE);

        return commandLineFxml;
    }

    private ContextMenu getTabContextMenu() {
        if (tabContextMenu == null) {
            tabContextMenu = new ContextMenu();

            MenuItem cmdLineTab = new MenuItem("Command Request");
            cmdLineTab.setOnAction(e -> addCommandRequest(e));

            MenuItem httpRequest = new MenuItem("Http Request");
            MenuItem saveRequest = new MenuItem("Save Request");
            MenuItem closeRequest = new MenuItem("Close Request");

            tabContextMenu.getItems().add(cmdLineTab);
            tabContextMenu.getItems().add(httpRequest);
            tabContextMenu.getItems().add(saveRequest);
            tabContextMenu.getItems().add(closeRequest);
        }

        return tabContextMenu;
    }

    @Subscribe
    public void showAlert(AlertEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("KTerminal - Error");
                alert.setHeaderText(event.getHeader());
                alert.setContentText(event.getMessage());

                alert.showAndWait();
            }
        });
    }

    @Subscribe
    public void showNotification(NotificationEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Notifications notificationBuilder = Notifications.create()
                        .title(event.getHeader())
                        .text(event.getMessage())
                        .hideAfter(Duration.seconds(5))
                        .position(Pos.TOP_RIGHT)
                        .threshold(5, Notifications.create().title("Threshold Notification"));
                notificationBuilder.owner(ktermParentPane);
                notificationBuilder.show();
            }
        });
    }
}
