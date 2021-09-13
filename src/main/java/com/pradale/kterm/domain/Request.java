package com.pradale.kterm.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;

import java.util.List;

@Data
@Builder
public class Request {
    private String value;

    @Singular
    private List<Parameter> parameters;

    public static Request getDefault() {
        return Request.builder().build();
    }
}
