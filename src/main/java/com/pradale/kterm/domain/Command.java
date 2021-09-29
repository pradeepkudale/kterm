package com.pradale.kterm.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@Jacksonized
public class Command {
    private String value;

    @Builder.Default
    private List<Parameter> parameters = new ArrayList<>();

    public static Command getDefault() {
        return Command.builder().build();
    }
}
