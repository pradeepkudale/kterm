package com.pradale.kterm.events;

import lombok.Data;

@Data
public class NotificationEvent {

    private String header;
    private String message;

    public NotificationEvent(String header,String message) {
        this.header = header;
        this.message = message;
    }
}
