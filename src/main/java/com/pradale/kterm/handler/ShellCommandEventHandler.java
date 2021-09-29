package com.pradale.kterm.handler;

import com.google.common.eventbus.EventBus;
import com.pradale.kterm.auth.AuthTypes;
import com.pradale.kterm.auth.Authenticator;
import com.pradale.kterm.control.Property;
import com.pradale.kterm.domain.AuthOption;
import com.pradale.kterm.domain.Command;
import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.Parameter;
import com.pradale.kterm.domain.auth.BasicAuthentication;
import com.pradale.kterm.domain.auth.HostAuthentication;
import com.pradale.kterm.domain.auth.NoAuthentication;
import com.pradale.kterm.domain.type.ShellCommand;
import com.pradale.kterm.events.AlertEvent;
import com.pradale.kterm.events.NotificationEvent;
import com.pradale.kterm.service.ShellCommandService;
import com.pradale.kterm.service.ssh.SSHClientProcess;
import com.pradale.kterm.utils.ApplicationUtils;
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
import lombok.SneakyThrows;
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
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

@Slf4j
@Component
public class ShellCommandEventHandler extends AbstractEventHandler {

    public static final String ADD_PROPERTY = "Add Property";
    public static final String REMOVE_PROPERTY = "Remove Property";

    @Autowired
    private Map<AuthTypes, Authenticator> hostAuthenticators;

    @Autowired
    private ShellCommandService shellCommandService;

    @Autowired
    private EventBus eventBus;

    @Autowired
    private ObjectFactory<SSHClientProcess> factory;

    @Autowired
    private ExecutorService service;

    public void initialize(Tab tab, BorderPane tabPanel, ShellCommand event) {
        ArrayList<Node> nodes = ComponentUtils.getAllChildControls(tabPanel);

        addTabHandlers(tab, nodes, event);
        addParametersTabHandlers(nodes, event);
        addAuthorizationTabHandlers(nodes, event);
        setDefaultAuthHandlers(nodes, event);
    }

    private void addTabHandlers(Tab tab, ArrayList<Node> nodes, ShellCommand shellCommand) {

        // Host name combo
        ComboBox<String> hostname = getComponent(nodes, "ctrlCmdCmbHostName", ComboBox.class);
        if (StringUtils.isNotBlank(shellCommand.getHost().getName())) {
            hostname.setValue(shellCommand.getHost().getName());
        }
        hostname.setOnAction(event -> {
            shellCommandService.updateHostName(shellCommand, hostname.getValue());
        });

        // Port number Text box
        TextField portNumber = getComponent(nodes, "ctrlCmdTxtPort", TextField.class);
        if (shellCommand.getHost().getPort() != 0) {
            portNumber.setText(String.valueOf(shellCommand.getHost().getPort()));
        }
        portNumber.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV && StringUtils.isNotBlank(portNumber.getText())) {
                shellCommandService.updateHostPort(shellCommand, NumberUtils.createInteger(portNumber.getText()));
            }
        });
        portNumber.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue.length() > 5) {
                    portNumber.setText(oldValue);
                }

                if (!newValue.matches("\\d*")) {
                    portNumber.setText(newValue.replaceAll("[^\\d]", ""));
                }
            }
        });

        // Command Text Box
        TextField command = getComponent(nodes, "ctrlCmdTxtCommand", TextField.class);
        if (StringUtils.isNotBlank(shellCommand.getCommand().getValue())) {
            command.setText(shellCommand.getCommand().getValue());
        }
        command.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV) {
                shellCommandService.updateCommand(shellCommand, command.getText());
            }
        });

        Button run = getComponent(nodes, "ctrlCmdBtnRun", Button.class);
        TextArea txtOutput = getComponent(nodes, "ctrlCmdTxtOutput", TextArea.class);
        run.setOnAction(e -> {
            if (validateShellCommand(shellCommand)) {
                service.execute(new Runnable() {
                    @SneakyThrows
                    @Override
                    public void run() {
                        KTermConsole output = new KTermConsole(txtOutput);

                        SSHClientProcess process = factory.getObject();
                        String cmd = "\n" + shellCommand.getHost().getName() + ":" + shellCommand.getHost().getPort() + ">" + shellCommand.getCommand().getValue() + "\n";

                        try {
                            output.write(cmd.getBytes());
                            process.execute(shellCommand, output);
                        } catch (IOException ex) {
                            log.error(ex.getMessage(), ex);
                            output.write(ex.getMessage().getBytes());
                        } finally {
                            output.close();
                        }
                    }
                });
            }
        });

        TableView2<Property> queryTableView = getComponent(nodes, "ctrlCmdTableQueryParam", TableView2.class);
        Button save = getComponent(nodes, "ctrlCmdBtnSave", Button.class);
        save.setOnAction(e -> {

            Command cmd = shellCommand.getCommand();
            cmd.setParameters(getParameters(queryTableView));

            if (StringUtils.isNotBlank(shellCommand.getFilePath())) {
                shellCommandService.save(shellCommand);
            } else {
                TextInputDialog dlg = new TextInputDialog(shellCommand.getId());
                dlg.setTitle("KTerminal - Save Shell Request");
                dlg.getDialogPane().setContentText("Save Shell Request?");

                dlg.show();
                dlg.resultProperty().addListener(r -> {
                    if (StringUtils.isNotBlank(dlg.getResult())) {
                        boolean isNewFile = shellCommand.isNew();
                        try {
                            String fileName = dlg.getResult();

                            if (!ApplicationUtils.validateFileName(fileName)) {
                                throw new IllegalArgumentException("Invalid file name : " + fileName);
                            }

                            shellCommand.setName(fileName);
                            shellCommand.setNew(false);
                            shellCommandService.save(shellCommand);
                            tab.setText(dlg.getResult());

                            eventBus.post(new NotificationEvent("Save Shell Request", "Shell command request saved successfully"));
                        } catch (Exception ex) {
                            shellCommand.setNew(isNewFile); // In case save fails. revert the state
                            shellCommand.setName(shellCommand.getId());
                            log.error(ex.getMessage(), ex);
                            eventBus.post(new NotificationEvent("Save Shell Request", ex.getMessage()));
                        }
                    }
                });
            }
        });
    }

    private void addParametersTabHandlers(ArrayList<Node> nodes, ShellCommand command) {
        TableView2<Property> queryTableView = getComponent(nodes, "ctrlCmdTableQueryParam", TableView2.class);
        queryTableView.rowHeaderVisibleProperty().set(true);

        Property initProperty = new Property("", "", "");
        List<Property> properties = command.getCommand().getParameters().stream().map(p -> Property.fromParameter(p)).collect(Collectors.toList());
        ObservableList<Property> data = FXCollections.observableArrayList(properties);
        command.getCommand().getParameters().add(initProperty.getParameter());

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
                    command.getCommand().getParameters().remove(property.getParameter());
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
                        Property rProperty = data.remove(index.intValue());
                        command.getCommand().getParameters().remove(rProperty.getParameter());
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

    private void addAuthorizationTabHandlers(ArrayList<Node> nodes, ShellCommand shellCommand) {
        CheckBox chkAuthDefault = getComponent(nodes, "ctrlCmdChkAuthDefault", CheckBox.class);
        AnchorPane authRightPane = getComponent(nodes, "ctrlCmdAuthRightPane", AnchorPane.class);

        ComboBox<AuthOption> cmbAuthOptions = getComponent(nodes, "ctrlCmdCmbAuthOptions", ComboBox.class);
        for (Authenticator authenticator : hostAuthenticators.values()) {
            cmbAuthOptions.getItems().add(new AuthOption(authenticator.getLabel(), authenticator.getAuthType()));
        }

        cmbAuthOptions.setOnAction(e -> {
            chkAuthDefault.setSelected(false);

            AuthOption selectedItem = cmbAuthOptions.getSelectionModel().getSelectedItem();
            HostAuthentication defaultAuth = shellCommand.getDefaultAuthentication();

            if (selectedItem.getAuthType() == AuthTypes.NO_AUTH) {
                addNoAuthControls(chkAuthDefault, authRightPane, defaultAuth);
            } else if (selectedItem.getAuthType() == AuthTypes.BASIC) {
                addBasicAuthControls(shellCommand, chkAuthDefault, authRightPane, selectedItem, defaultAuth);
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

    private void addNoAuthControls(CheckBox chkAuthDefault, AnchorPane authRightPane, HostAuthentication defaultAuth) {
        authRightPane.getChildren().clear();

        if (defaultAuth instanceof NoAuthentication) {
            chkAuthDefault.setSelected(true);
        }
    }

    private void addBasicAuthControls(ShellCommand shellCommand, CheckBox chkAuthDefault, AnchorPane authRightPane, AuthOption selectedItem, HostAuthentication defaultAuth) {
        authRightPane.getChildren().clear();

        Authenticator authenticator = hostAuthenticators.get(selectedItem.getAuthType());
        Pane pane = ComponentUtils.loadFXML(authenticator.getResource());
        pane.setMaxHeight(Double.MAX_VALUE);
        pane.setMaxWidth(Double.MAX_VALUE);
        authRightPane.getChildren().add(pane);

        addBasicAuthHandlers(pane, shellCommand);

        if (defaultAuth instanceof BasicAuthentication) {
            chkAuthDefault.setSelected(true);
        }
    }

    private void setDefaultAuthHandlers(ArrayList<Node> nodes, ShellCommand shellCommand) {
        CheckBox chkAuthDefault = getComponent(nodes, "ctrlCmdChkAuthDefault", CheckBox.class);
        AnchorPane authRightPane = getComponent(nodes, "ctrlCmdAuthRightPane", AnchorPane.class);
        ComboBox<AuthOption> cmbAuthOptions = getComponent(nodes, "ctrlCmdCmbAuthOptions", ComboBox.class);

        HostAuthentication defaultAuth = shellCommand.getDefaultAuthentication();

        if (defaultAuth instanceof NoAuthentication) {
            Authenticator auth = hostAuthenticators.get(AuthTypes.NO_AUTH);
            cmbAuthOptions.setValue(new AuthOption(auth.getLabel(), auth.getAuthType()));

            addNoAuthControls(chkAuthDefault, authRightPane, defaultAuth);
        } else if (defaultAuth instanceof BasicAuthentication) {
            Authenticator auth = hostAuthenticators.get(AuthTypes.BASIC);
            AuthOption selectedItem = new AuthOption(auth.getLabel(), auth.getAuthType());
            cmbAuthOptions.setValue(selectedItem);
            addBasicAuthControls(shellCommand, chkAuthDefault, authRightPane, selectedItem, defaultAuth);
        }
    }


    public void addBasicAuthHandlers(Pane pane, ShellCommand shellCommand) {
        ArrayList<Node> nodes = ComponentUtils.getAllChildControls(pane);
        TextField txtUserName = getComponent(nodes, "txtBasicAuthUserName", TextField.class);
        TextField txtPassword = getComponent(nodes, "txtBasicAuthPassword", TextField.class);

        BasicAuthentication authentication = shellCommand.getHostAuthentication(BasicAuthentication.class);

        txtUserName.setText(authentication != null ? authentication.getUserName() : "");
        txtUserName.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV) {
                shellCommandService.updateUserName(shellCommand, BasicAuthentication.class, txtUserName.getText());
            }
        });

        txtPassword.setText(authentication != null ? authentication.getPassword() : "");
        txtPassword.focusedProperty().addListener((ov, oldV, newV) -> {
            if (!newV) {
                shellCommandService.updatePassword(shellCommand, BasicAuthentication.class, txtPassword.getText());
            }
        });
    }

    private boolean validateShellCommand(ShellCommand shellCommand) {
        Host host = shellCommand.getHost();
        HostAuthentication authentication = shellCommand.getDefaultAuthentication();
        Command command = shellCommand.getCommand();
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

    private List<Parameter> getParameters(TableView2<Property> queryTableView) {
        return queryTableView
                .getItems()
                .stream()
                .map(p -> p.getParameter())
                .collect(Collectors.toList());
    }
}
