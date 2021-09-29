package com.pradale.kterm.domain;

import lombok.Data;

@Data
public class Parameter {
    private String key;
    private String value;
    private String description;
    private boolean visible;

}
