package org.example.filter;

import java.util.Objects;

public class UserFilters {

    private UserFilters() {}

    public static UserFilter byUsername(String username) {
        Objects.requireNonNull(username);
        return user -> user.username().equals(username);
    }

    public static UserFilter byUsernameContains(String substring) {
        Objects.requireNonNull(substring);
        return user -> user.username().toLowerCase().contains(substring.toLowerCase());
    }

    public static UserFilter byEmail(String email) {
        Objects.requireNonNull(email);
        return user -> user.email().equals(email);
    }

    public static UserFilter byEmailDomain(String domain) {
        Objects.requireNonNull(domain);
        String suffix = domain.startsWith("@") ? domain : "@" + domain;
        return user -> user.email().endsWith(suffix);
    }

    public static UserFilter byFullNameContains(String substring) {
        Objects.requireNonNull(substring);
        return user -> user.fullname().toLowerCase().contains(substring.toLowerCase());
    }
}
