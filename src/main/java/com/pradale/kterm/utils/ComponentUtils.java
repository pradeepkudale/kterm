package com.pradale.kterm.utils;

import com.pradale.kterm.domain.type.Item;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.*;
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

    public static TreeItem<Item> getTreeViewItem(TreeItem<Item> treeItem, String value) {
        if (treeItem != null && treeItem.getValue().getId().equals(value))
            return treeItem;

        for (TreeItem<Item> item : treeItem.getChildren()) {
            TreeItem sItem = getTreeViewItem(item, value);
            if (sItem != null)
                return sItem;

        }
        return null;
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