package com.pradale.kterm.handler;

import com.google.common.eventbus.EventBus;
import com.pradale.kterm.auth.AuthTypes;
import com.pradale.kterm.auth.Authenticator;
import com.pradale.kterm.control.Property;
import com.pradale.kterm.domain.AuthOption;
import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.Request;
import com.pradale.kterm.domain.auth.BasicAuthentication;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.auth.NoAuthentication;
import com.pradale.kterm.domain.command.ShellCommand;
import com.pradale.kterm.events.AlertEvent;
import com.pradale.kterm.events.NotificationEvent;
import com.pradale.kterm.service.SSHClientService;
import com.pradale.kterm.service.ShellCommandService;
import com.pradale.kterm.service.ssh.SSHClientProcess;
import com.pradale.kterm.utils.ComponentUtils;
import com.pradale.kterm.utils.KTermConsole;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.controlsfx.control.action.ActionUtils;
import org.controlsfx.control.tableview2.TableColumn2;
import org.controlsfx.control.tableview2.TableView2;
import org.controlsfx.control.tableview2.cell.TextField2TableCell;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
public class ShellCommandEventHandler extends AbstractEventHandler {

    public static final String ADD_PROPERTY = "Add Property";
    public static final String REMOVE_PROPERTY = "Remove Property";

    @Autowired
    private Map<AuthTypes, Authenticator> hostAuthenticators;

    @Autowired
    private SSHClientService sshClientService;

    @Autowired
    private ShellCommandService shellCommandService;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private ObjectFactory<SSHClientProcess> factory;

    public void initialize(Tab tab, BorderPane tabPanel, ShellCommand event) {
        ArrayList<Node> nodes = ComponentUtils.getAllChildControls(tabPanel);

        populateParametersTab(nodes, event);
        populateAuthorizationTab(nodes, event);
        addHandlers(tab, nodes, event);
    }

    private void addHandlers(Tab tab, ArrayList<Node> nodes, ShellCommand shellCommand) {
        TextField txtCommand = getComponent(nodes, "ctrlCmdTxtCommand", TextField.class);
        TextField txtPort = getComponent(nodes, "ctrlCmdTxtPort", TextField.class);
        ComboBox<String> cmbHostName = getComponent(nodes, "ctrlCmdCmbHostName", ComboBox.class);
        Button btnRun = getComponent(nodes, "ctrlCmdBtnRun", Button.class);
        Button btnSave = getComponent(nodes, "ctrlCmdBtnSave", Button.class);
        TextArea txtOutput = getComponent(nodes, "ctrlCmdTxtOutput", TextArea.class);

        txtCommand.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV) {
                shellCommandService.updateCommand(shellCommand, txtCommand.getText());
            }
        });

        cmbHostName.setOnAction(event -> {
            shellCommandService.updateHostName(shellCommand, cmbHostName.getValue());
        });

        txtPort.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV && StringUtils.isNotBlank(txtPort.getText())) {
                shellCommandService.updateHostPort(shellCommand, NumberUtils.createInteger(txtPort.getText()));
            }
        });

        txtPort.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.length() > 5) {
                    txtPort.setText(oldValue);
                }

                if (!newValue.matches("\\d*")) {
                    txtPort.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        btnRun.setOnAction(e -> {
            if (validateShellCommand(shellCommand)) {

                ExecutorService es = Executors.newCachedThreadPool();

                es.execute(new Runnable() {
                    @Override
                    public void run() {
                        BlockingQueue<String> messageQueue = new ArrayBlockingQueue<>(1);
                        KTermConsole output = new KTermConsole(txtOutput);


                        SSHClientProcess process = factory.getObject();
                        String cmd = "\n" + shellCommand.getHost().getName() + ":" + shellCommand.getHost().getPort() + ">" + shellCommand.getCommand().getValue() + "\n";

                        try {
                            output.write(cmd.getBytes());
                            process.execute(shellCommand, output);
                        } catch (IOException ex) {
                            log.error(ex.getMessage(), ex);
                            try {
                                output.write(ex.getMessage().getBytes());
                            } catch (IOException exc) {
                            }
                        }
                    }
                });
            }
        });

        btnSave.setOnAction(e -> {
            TextInputDialog dlg = new TextInputDialog(shellCommand.getId());
            dlg.setTitle("KTerminal - Save Shell Request");
            dlg.getDialogPane().setContentText("Save Shell Request?");

            dlg.show();
            dlg.resultProperty().addListener(r -> {
                if (StringUtils.isNotBlank(dlg.getResult())) {
                    boolean isNewFile = shellCommand.isNew();
                    try {
                        shellCommand.setId(dlg.getResult());
                        shellCommand.setNew(false);
                        shellCommandService.save(shellCommand);
                        tab.setText(dlg.getResult());
                    } catch (Exception ex) {
                        shellCommand.setNew(isNewFile); // In case save fails. revert the state
                        log.error(ex.getMessage(), ex);
                        eventBus.post(new NotificationEvent("Save Shell Request", ex.getMessage()));
                    }
                }
            });
        });
    }

    private void populateParametersTab(ArrayList<Node> nodes, ShellCommand event) {
        TableView2<Property> queryTableView = getComponent(nodes, "ctrlCmdTableQueryParam", TableView2.class);
        queryTableView.rowHeaderVisibleProperty().set(true);

        ObservableList<Property> data = FXCollections.observableArrayList(new Property("", "", ""));

        TableColumn2<Property, String> key = new TableColumn2<>("Key");
        TableColumn2<Property, String> value = new TableColumn2<>("Value");
        TableColumn2<Property, String> details = new TableColumn2<>("Details");

        key.setCellValueFactory(p -> p.getValue().keyProperty());
        key.setCellFactory(TextField2TableCell.forTableColumn());
        key.setPrefWidth(300);

        value.setCellValueFactory(p -> p.getValue().valueProperty());
        value.setCellFactory(TextField2TableCell.forTableColumn());
        value.setPrefWidth(300);

        details.setCellValueFactory(p -> p.getValue().detailsProperty());
        details.setCellFactory(TextField2TableCell.forTableColumn());
        details.setPrefWidth(400);

        MenuItem mnuDel = new MenuItem(REMOVE_PROPERTY);
        mnuDel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                ObservableList<Property> items = queryTableView.getItems();
                Property property = queryTableView.getSelectionModel().getSelectedItem();
                if (property != null) {
                    items.remove(property);
                    addNewRow(data);// adds a new row if the removed row was the last one
                }
            }
        });
        MenuItem mnuUnDel = new MenuItem(ADD_PROPERTY);
        mnuUnDel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent t) {
                addNewRow(data);
            }
        });

        queryTableView.setContextMenu(new ContextMenu(mnuDel, mnuUnDel));
        queryTableView.getColumns().addAll(key, value, details);

        queryTableView.setRowHeaderContextMenuFactory((Integer index, Property property) -> {
            ContextMenu contextMenu = ActionUtils.createContextMenu(Arrays.asList());
            Integer rowsCount = data.size();

            if (index < rowsCount) {
                final MenuItem menuItem = new MenuItem("Remove  row");
                menuItem.setOnAction(e -> {
                    if (index >= 0) {
                        ObservableList<Property> items = queryTableView.getItems();
                        data.remove(index.intValue());
                        addNewRow(data); // adds a new row if the removed row was the last one
                    }
                });
                contextMenu.getItems().addAll(menuItem);
            }

            final MenuItem menuItemAdd = new MenuItem(ADD_PROPERTY);
            menuItemAdd.setOnAction(e -> addNewRow(data));
            contextMenu.getItems().addAll(menuItemAdd);

            return contextMenu;
        });

        queryTableView.setItems(data);
        queryTableView.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                addNewRow(data);
            }
        });
    }

    private void populateAuthorizationTab(ArrayList<Node> nodes, ShellCommand shellCommand) {
        CheckBox chkAuthDefault = getComponent(nodes, "ctrlCmdChkAuthDefault", CheckBox.class);
        AnchorPane authLeftPane = getComponent(nodes, "ctrlCmdAuthLeftPane", AnchorPane.class);
        AnchorPane authRightPane = getComponent(nodes, "ctrlCmdAuthRightPane", AnchorPane.class);

        ComboBox<AuthOption> cmbAuthOptions = getComponent(nodes, "ctrlCmdCmbAuthOptions", ComboBox.class);
        for (Authenticator authenticator : hostAuthenticators.values()) {
            cmbAuthOptions.getItems().add(new AuthOption(authenticator.getLabel(), authenticator.getAuthType()));
        }

        cmbAuthOptions.setOnAction(e -> {
            AuthOption selectedItem = cmbAuthOptions.getSelectionModel().getSelectedItem();

            if (selectedItem.getAuthType() == AuthTypes.NO_AUTH) {
                authRightPane.getChildren().clear();
                return;
            } else {
                Authenticator authenticator = hostAuthenticators.get(selectedItem.getAuthType());
                Pane pane = ComponentUtils.loadFXML(authenticator.getResource());
                pane.setMaxHeight(Double.MAX_VALUE);
                pane.setMaxWidth(Double.MAX_VALUE);
                authRightPane.getChildren().add(pane);

                if (authenticator.getAuthType() == AuthTypes.BASIC) {
                    addBasicAuthHandlers(pane, shellCommand);
                }
            }
        });

        chkAuthDefault.setOnAction(e -> {
            AuthOption authOption = cmbAuthOptions.getSelectionModel().getSelectedItem();

            if (authOption.getAuthType() == AuthTypes.NO_AUTH) {
                shellCommandService.updateDefaultAuthentication(shellCommand, NoAuthentication.class);
            } else if (authOption.getAuthType() == AuthTypes.BASIC) {
                shellCommandService.updateDefaultAuthentication(shellCommand, BasicAuthentication.class);
            }
        });
    }

    public void addBasicAuthHandlers(Pane pane, ShellCommand shellCommand) {
        ArrayList<Node> nodes = ComponentUtils.getAllChildControls(pane);
        TextField txtUserName = getComponent(nodes, "txtBasicAuthUserName", TextField.class);
        TextField txtPassword = getComponent(nodes, "txtBasicAuthPassword", TextField.class);

        txtUserName.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV) {
                shellCommandService.updateUserName(shellCommand, BasicAuthentication.class, txtUserName.getText());
            }
        });

        txtPassword.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV) {
                shellCommandService.updatePassword(shellCommand, BasicAuthentication.class, txtPassword.getText());
            }
        });
    }

    private boolean validateShellCommand(ShellCommand shellCommand) {
        Host host = shellCommand.getHost();
        HostAuthentication authentication = shellCommand.getDefaultAuthentication();
        Request command = shellCommand.getCommand();
        String username = authentication.getUserName();
        List<String> messages = new ArrayList<>(5);

        if (StringUtils.isBlank(host.getName())) {
            messages.add("Please enter server name");
        }

        if (host.getPort() == 0) {
            messages.add("Please enter host port");
        }

        if (StringUtils.isBlank(username)) {
            messages.add("Please enter username");
        }

        if (StringUtils.isBlank(command.getValue())) {
            messages.add("Please enter command to execute");
        }

        if (!messages.isEmpty()) {
            String message = String.join("\n", messages);
            eventBus.post(new AlertEvent("Shell Command", message));
            return false;
        }

        return true;
    }

    private void addNewRow(ObservableList<Property> data) {
        if (data.isEmpty()) {
            data.add(new Property("", "", ""));
        } else {
            Property property = data.get(data.size() - 1);
            if (!property.isEmpty()) {
                data.add(new Property("", "", ""));
            }
        }
    }

    private <T> T getComponent(List<Node> nodes, String id, Class<T> classz) {
        return (T) nodes.stream().filter(node -> id.equals(node.getId())).findFirst().orElse(null);
    }
}
