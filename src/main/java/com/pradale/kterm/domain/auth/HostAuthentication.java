package com.pradale.kterm.domain.auth;

import com.fasterxml.jackson.annotation.JsonSubTypes;

@JsonSubTypes({
        @JsonSubTypes.Type(name = "noAuthentication", value = NoAuthentication.class),
        @JsonSubTypes.Type(name = "basicAuthentication", value = BasicAuthentication.class)
})
public interface HostAuthentication extends Authentication {
}
