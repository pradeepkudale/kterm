package com.pradale.kterm.domain.type;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.pradale.kterm.domain.Command;
import com.pradale.kterm.domain.Host;
import com.pradale.kterm.domain.auth.Authentication;

import java.util.Set;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface Terminal {

    Host getHost();

    Command getCommand();

    Set<? extends Authentication> getAuthentications();

    Authentication getDefaultAuthentication();

    boolean isNew();
}
