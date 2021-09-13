package com.pradale.kterm.domain;

import com.pradale.kterm.auth.AuthTypes;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthOption {
    private String label;
    private AuthTypes authType;

    @Override
    public String toString() {
        return label;
    }
}
