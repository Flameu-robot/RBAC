package org.example.entity;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

public record AssignmentMetadata(
        String assignedBy,
        String assignedAt,
        String reason
) {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public AssignmentMetadata {
        Objects.requireNonNull(assignedBy, "assignedBy cannot be null");
        Objects.requireNonNull(assignedAt, "assignedAt cannot be null");

        if (assignedBy.isBlank()) {
            throw new IllegalArgumentException("assignedBy cannot be blank");
        }
    }

    public static AssignmentMetadata now(String assignedBy, String reason) {
        String timestamp = LocalDateTime.now().format(FORMATTER);
        return new AssignmentMetadata(assignedBy, timestamp, reason);
    }

    public String format() {
        String reasonText = (reason != null && !reason.isBlank()) ? reason : "No reason provided";
        return String.format("Assigned by: %s at %s. Reason: %s", assignedBy, assignedAt, reasonText);
    }
}