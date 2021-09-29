package com.pradale.kterm.controller;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.pradale.kterm.constant.ApplicationConstant;
import com.pradale.kterm.control.ClickableTreeCell;
import com.pradale.kterm.domain.type.Item;
import com.pradale.kterm.domain.type.ShellCommand;
import com.pradale.kterm.domain.type.TextItem;
import com.pradale.kterm.events.AlertEvent;
import com.pradale.kterm.events.LoadShellCommandEvent;
import com.pradale.kterm.events.LoadTreeViewShellCommand;
import com.pradale.kterm.events.NotificationEvent;
import com.pradale.kterm.handler.ShellCommandEventHandler;
import com.pradale.kterm.service.InitializeService;
import com.pradale.kterm.utils.ApplicationUtils;
import com.pradale.kterm.utils.ComponentUtils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import javafx.util.Duration;
import lombok.SneakyThrows;
import org.controlsfx.control.Notifications;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Controller;

import java.net.URL;
import java.util.Optional;

@Controller
public class KTermController {

    @Autowired
    private ApplicationContext applicationContext;

    @Autowired
    private InitializeService initializeService;

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

    @FXML
    private TreeView<Item> ctrlTreeView;

    private ContextMenu tabContextMenu;

    private ObservableList<Item> treeItems = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        eventBus.register(this);

        // Initialize tree view items
        initializeTreeViewItems();

        // Load all saved items
        initializeService.loadTabPane();
    }

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
            tab.setText(shellCommand.getName());
            tab.setId(shellCommand.getId());
        }

        commandRequestEventHandler.initialize(tab, tabPanel, shellCommand);
        ctrlTabPanelParent.getTabs().add(tab);
    }

    public void addNewShellCommand(ActionEvent actionEvent) {
        loadCommandTab(ShellCommand.getDefault(true));
    }

    @Subscribe
    public void addTreeViewShellCommand(LoadTreeViewShellCommand loadTreeViewShellCommand) {
        TreeItem<Item> shellCommands = ComponentUtils.getTreeViewItem(ctrlTreeView.getRoot(), ApplicationConstant.SHELL_COMMANDS);

        ShellCommand shellCommand = loadTreeViewShellCommand.getShellCommand();
        TreeItem<Item> newItem = new TreeItem<>(shellCommand);

        shellCommands.getChildren().add(newItem);
    }

    @Subscribe
    public void addShellCommand(LoadShellCommandEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                ShellCommand command = event.getShellCommand();
                ObservableList<Tab> tabs = ctrlTabPanelParent.getTabs();

                Optional<Tab> tab = tabs.stream().filter(tb -> tb.getId().equalsIgnoreCase(command.getId())).findFirst();

                if (!tab.isPresent()) {
                    loadCommandTab(command);
                }
            }
        });
    }

    public void addTreeViewHistory() {

    }

    @Subscribe
    public void showAlert(AlertEvent event) {
        Platform.runLater(new Runnable() {
            @Override
            public void run() {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle(ApplicationConstant.KTERMINAL_ERROR);
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
                        .threshold(5, Notifications.create().title(ApplicationConstant.THRESHOLD_NOTIFICATION));
                notificationBuilder.owner(ktermParentPane);
                notificationBuilder.show();
            }
        });
    }

    private void initializeTreeViewItems() {
        TreeItem<Item> rootItem = new TreeItem<>(TextItem.getTextItem(ApplicationConstant.ROOT));

        TreeItem<Item> shellCommands = new TreeItem<>(TextItem.getTextItem((ApplicationConstant.SHELL_COMMANDS)));
        TreeItem<Item> history = new TreeItem<>(TextItem.getTextItem(ApplicationConstant.HISTORY));

        shellCommands.getChildren().add(history);
        rootItem.getChildren().add(shellCommands);

        ctrlTreeView.setRoot(rootItem);
        ctrlTreeView.setShowRoot(false);

        ctrlTreeView.setCellFactory(new Callback<TreeView<Item>, TreeCell<Item>>() {
            @Override
            public TreeCell<Item> call(TreeView<Item> treeView) {
                return new ClickableTreeCell(eventBus);
            }
        });
        ctrlTreeView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
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

            MenuItem cmdLineTab = new MenuItem(ApplicationConstant.COMMAND_REQUEST);
            cmdLineTab.setOnAction(e -> addNewShellCommand(e));

            MenuItem httpRequest = new MenuItem(ApplicationConstant.HTTP_REQUEST);
            MenuItem saveRequest = new MenuItem(ApplicationConstant.SAVE_REQUEST);
            MenuItem closeRequest = new MenuItem(ApplicationConstant.CLOSE_REQUEST);

            tabContextMenu.getItems().add(cmdLineTab);
            tabContextMenu.getItems().add(httpRequest);
            tabContextMenu.getItems().add(saveRequest);
            tabContextMenu.getItems().add(closeRequest);
        }
        return tabContextMenu;
    }

}
