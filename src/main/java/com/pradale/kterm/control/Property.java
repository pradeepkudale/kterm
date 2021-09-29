package com.pradale.kterm.control;

import com.pradale.kterm.domain.Parameter;
import javafx.beans.property.SimpleStringProperty;
import org.apache.commons.lang3.StringUtils;

public class Property {
    private SimpleStringProperty key;
    private SimpleStringProperty value;
    private SimpleStringProperty details;
    private Parameter parameter;

    public Property(String key, String value, String details) {
        this.key = new SimpleStringProperty(key);
        this.value = new SimpleStringProperty(value);
        this.details = new SimpleStringProperty(details);
    }

    public String getKey() {
        return key.get();
    }

    public void setKey(String key) {
        this.key.set(key);
    }

    public SimpleStringProperty keyProperty() {
        return key;
    }

    public String getValue() {
        return value.get();
    }

    public void setValue(String value) {
        this.value.set(value);
    }

    public SimpleStringProperty valueProperty() {
        return value;
    }

    public String getDetails() {
        return details.get();
    }

    public void setDetails(String details) {
        this.details.set(details);
    }

    public SimpleStringProperty detailsProperty() {
        return details;
    }

    public boolean isEmpty() {
        return StringUtils.isBlank(key.getValue()) &&
                StringUtils.isBlank(value.getValue()) &&
                StringUtils.isBlank(details.getValue());
    }

    public Parameter getParameter() {
        if (parameter == null) {
            parameter = new Parameter();
        }
        parameter.setKey(key.get());
        parameter.setValue(value.get());
        parameter.setDescription(details.get());
        return parameter;
    }

    public static Property fromParameter(Parameter parameter) {
        return new Property(parameter.getKey(), parameter.getValue(), parameter.getDescription());
    }
}