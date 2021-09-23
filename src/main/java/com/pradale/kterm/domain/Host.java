package com.pradale.kterm.domain;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

@Data
@Builder
@Jacksonized
public class Host {
    private String name;
    private int port;

}
