package org.example.entity;

import java.util.regex.Pattern;

public record User(
        String username,
        String fullname,
        String email
) {
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]+$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile(".+@.+\\..+");

    public User {
        validate(username, fullname, email);
    }

    private static void validate(String username, String fullname, String email) {
        notNullValidation(username, fullname, email);

        if (!USERNAME_PATTERN.matcher(username).matches()) {
            throw new IllegalArgumentException("Invalid username: " + username);
        }

        if (username.length() < 3 || username.length() > 20) {
            throw new IllegalArgumentException("Username must be between 3 and 20 characters long");
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new IllegalArgumentException("Invalid email: " + email);
        }
    }

    private static void notNullValidation(String username, String fullname, String email) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("Username is empty");
        }

        if (fullname == null || fullname.isBlank()) {
            throw new IllegalArgumentException("Fullname is empty");
        }

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is empty");
        }
    }

    public String format() {
        return username + " (" + fullname + ") <" + email + ">";
    }
}