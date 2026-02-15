package org.example.entity;

import java.util.Objects;

public record Permission(
        String name,
        String resource,
        String description
)
{
    public Permission {
        Objects.requireNonNull(name, "Name cannot be null");
        Objects.requireNonNull(resource, "Resource cannot be null");
        Objects.requireNonNull(description, "Description cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException("Name cannot be blank");
        }
        if (resource.isBlank()) {
            throw new IllegalArgumentException("Resource cannot be blank");
        }
        if (description.isBlank()) {
            throw new IllegalArgumentException("Description cannot be blank");
        }

        name = name.toUpperCase();
        resource = resource.toLowerCase();

        if (name.contains(" ")) {
            throw new IllegalArgumentException("Name must not contain spaces");
        }
    }

    public String format() {
        return name + " on " + resource + ": " + description;
    }

    public boolean matches(String namePattern, String resourcePattern) {
        boolean nameMatches = (namePattern == null || namePattern.isBlank())
                || name.contains(namePattern.toUpperCase());

        boolean resourceMatches = (resourcePattern == null || resourcePattern.isBlank())
                || resource.contains(resourcePattern.toLowerCase());

        return nameMatches && resourceMatches;
    }
}