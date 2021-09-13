package com.pradale.kterm.utils;

import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;
import lombok.SneakyThrows;
import org.springframework.core.io.Resource;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ComponentUtils {

    public static ArrayList<Node> getAllChildControls(Parent root) {
        ArrayList<Node> nodes = new ArrayList<>();
        getAllChildControls(root, nodes);
        return nodes;
    }

    @SneakyThrows
    public static <T> T loadFXML(Resource resource) {
        return FXMLLoader.load(resource.getURL());
    }

    private static void getAllChildControls(Parent parent, ArrayList<Node> nodes) {

        List<Node> children = Collections.EMPTY_LIST;
        if (parent instanceof ButtonBar) {
            children = ((ButtonBar) parent).getButtons();
        } else if (parent instanceof TabPane) {
            for (Tab tab : ((TabPane) parent).getTabs()) {
                Node tabContent = tab.getContent();
                if (tabContent instanceof Parent) {
                    getAllChildControls((Parent) tab.getContent(), nodes);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        } else if (parent instanceof SplitPane) {
            for (Node node : ((SplitPane) parent).getItems()) {
                if (node instanceof AnchorPane) {
                    getAllChildControls((Parent) node, nodes);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        } else {
            children = parent.getChildrenUnmodifiable();
        }

        nodes.add(parent);
        for (Node node : children) {
            nodes.add(node);
            if (node instanceof Parent) {
                getAllChildControls((Parent) node, nodes);
            }
        }
    }
}