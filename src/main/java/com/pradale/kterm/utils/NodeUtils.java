package com.pradale.kterm.utils;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.SplitPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.AnchorPane;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodeUtils {

    public static ArrayList<Node> getAllNodes(Parent root) {
        ArrayList<Node> nodes = new ArrayList<>();
        addAllDescendents(root, nodes);
        return nodes;
    }

    private static void addAllDescendents(Parent parent, ArrayList<Node> nodes) {

        List<Node> children = Collections.EMPTY_LIST;
        if (parent instanceof ButtonBar) {
            children = ((ButtonBar) parent).getButtons();
        } else if (parent instanceof TabPane) {
            for (Tab tab : ((TabPane) parent).getTabs()) {
                Node tabContent = tab.getContent();
                if (tabContent instanceof Parent) {
                    addAllDescendents((Parent) tab.getContent(), nodes);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        } else if (parent instanceof SplitPane) {
            for (Node node : ((SplitPane) parent).getItems()) {
                if (node instanceof AnchorPane) {
                    addAllDescendents((Parent) node, nodes);
                } else {
                    throw new UnsupportedOperationException();
                }
            }
        } else {
            children = parent.getChildrenUnmodifiable();
        }

        // Add nodes.
        for (Node node : children) {
            nodes.add(node);
            if (node instanceof Parent) {
                addAllDescendents((Parent) node, nodes);
            }
        }
    }
}