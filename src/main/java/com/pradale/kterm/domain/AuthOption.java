package com.pradale.kterm.domain;

import com.pradale.kterm.auth.AuthTypes;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

@Data
@AllArgsConstructor
public class AuthOption {
    private String label;
    private AuthTypes authType;

    @Override
    public String toString() {
        return label;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        AuthOption that = (AuthOption) o;

        return new EqualsBuilder().append(authType, that.authType).isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37).append(authType).toHashCode();
    }
}
