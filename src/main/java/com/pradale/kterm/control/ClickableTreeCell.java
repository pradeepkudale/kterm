package com.pradale.kterm.control;

import com.google.common.eventbus.EventBus;
import com.pradale.kterm.domain.type.Item;
import com.pradale.kterm.domain.type.ShellCommand;
import com.pradale.kterm.events.LoadShellCommandEvent;
import javafx.event.EventHandler;
import javafx.scene.control.TreeCell;
import javafx.scene.input.MouseEvent;

public class ClickableTreeCell extends TreeCell<Item> {

    private EventBus eventBus;

    public ClickableTreeCell(EventBus eventBus) {
        this.eventBus = eventBus;
        eventBus.register(this);
        setOnMouseClicked(new EventHandler<MouseEvent>() {
            @Override
            public void handle(MouseEvent event) {
                // Handle double-clicks on non-empty cells:
                if (event.getClickCount() == 2 && !isEmpty()) {
                    Item item = getItem();
                    if (item instanceof ShellCommand) {
                        eventBus.post(LoadShellCommandEvent.builder().shellCommand((ShellCommand) getItem()).build());
                    }
                }
            }
        });
    }

    @Override
    protected void updateItem(Item item, boolean empty) {
        super.updateItem(item, empty);
        if (empty) {
            setText(null);
        } else {
            setText(item.getName());
        }
    }
}
