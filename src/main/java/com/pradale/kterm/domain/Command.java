package com.pradale.kterm.domain;

import lombok.Builder;
import lombok.Data;
import lombok.Singular;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@Builder
@Jacksonized
public class Command {
    private String value;

    @Singular
    private List<Parameter<?>> parameters;

    public static Command getDefault() {
        return Command.builder().build();
    }
}
