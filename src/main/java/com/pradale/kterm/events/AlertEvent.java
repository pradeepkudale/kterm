package com.pradale.kterm.events;

import lombok.Data;

@Data
public class AlertEvent {

    private String header;
    private String message;

    public AlertEvent(String header,String message) {
        this.message = message;
    }
}
