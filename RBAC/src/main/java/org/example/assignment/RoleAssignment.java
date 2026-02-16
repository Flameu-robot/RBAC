package org.example.assignment;

import org.example.entity.AssignmentMetadata;
import org.example.entity.Role;
import org.example.entity.User;

public interface RoleAssignment {
    String assignmentId();
    User user();
    Role role();
    AssignmentMetadata metadata();
    boolean isActive();
    String assignmentType();
}