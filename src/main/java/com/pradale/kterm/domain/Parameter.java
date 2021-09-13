package com.pradale.kterm.domain;

import lombok.Data;

@Data
public class Parameter<T> {
    private String key;
    private T value;
    private String description;
    private boolean visible;

}
