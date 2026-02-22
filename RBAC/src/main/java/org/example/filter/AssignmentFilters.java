package org.example.filter;

import org.example.entity.Role;
import org.example.assignment.RoleAssignment;
import org.example.assignment.TemporaryAssignment;
import org.example.entity.User;
import java.util.Objects;

public class AssignmentFilters {

    private AssignmentFilters() {}

    public static AssignmentFilter byUser(User user) {
        Objects.requireNonNull(user);
        return assignment -> assignment.user().equals(user);
    }

    public static AssignmentFilter byUsername(String username) {
        Objects.requireNonNull(username);
        return assignment -> assignment.user().username().equals(username);
    }

    public static AssignmentFilter byRole(Role role) {
        Objects.requireNonNull(role);
        return assignment -> assignment.role().equals(role);
    }

    public static AssignmentFilter byRoleName(String roleName) {
        Objects.requireNonNull(roleName);
        return assignment -> assignment.role().getName().equals(roleName);
    }

    public static AssignmentFilter activeOnly() {
        return RoleAssignment::isActive;
    }

    public static AssignmentFilter inactiveOnly() {
        return assignment -> !assignment.isActive();
    }

    public static AssignmentFilter byType(String type) {
        Objects.requireNonNull(type);
        return assignment -> assignment.assignmentType().equals(type);
    }

    public static AssignmentFilter assignedBy(String username) {
        Objects.requireNonNull(username);
        return assignment -> assignment.metadata().assignedBy().equals(username);
    }

    public static AssignmentFilter assignedAfter(String date) {
        Objects.requireNonNull(date);
        return assignment -> assignment.metadata().assignedAt().compareTo(date) > 0;
    }

    public static AssignmentFilter expiringBefore(String date) {
        Objects.requireNonNull(date);
        return assignment -> {
            if (assignment instanceof TemporaryAssignment temp) {
                return temp.getExpiresAt().compareTo(date) < 0;
            }
            return false;
        };
    }
}