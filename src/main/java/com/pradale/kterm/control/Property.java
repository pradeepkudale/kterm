package com.pradale.kterm.control;

import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.StringUtils;

public class Property {
    private SimpleStringProperty key;
    private SimpleStringProperty value;
    private SimpleStringProperty details;

    public Property(String key, String value, String details) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
        this.details = new SimpleStringProperty(details);
    }

    public String getKey() {
        return key.get();
    }

    public SimpleStringProperty keyProperty() {
        return key;
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public String getValue() {
        return value.get();
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public String getDetails() {
        return details.get();
    }

    public SimpleStringProperty detailsProperty() {
        return details;
    }

    public void setDetails(String details) {
        this.details.set(details);
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(key.getValue()) &&
                StringUtils.isBlank(value.getValue()) &&
                StringUtils.isBlank(details.getValue());
    }
}