package com.pradale.kterm.domain.type;

public interface Item {
    String getId();

    String getName();

    String getDisplayPath();

    String getFilePath();

    default ItemType getType() {
        return ItemType.UN_DEFINED;
    }
}
