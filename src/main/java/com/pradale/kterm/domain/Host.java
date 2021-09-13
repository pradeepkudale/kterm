package com.pradale.kterm.domain;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Host {
    private String name;
    private int port;

}
