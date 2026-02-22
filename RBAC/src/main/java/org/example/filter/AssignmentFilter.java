package org.example.filter;

import org.example.assignment.RoleAssignment;

@FunctionalInterface
public interface AssignmentFilter {
    boolean test(RoleAssignment assignment);

    default AssignmentFilter and(AssignmentFilter other) {
        return assignment -> this.test(assignment) && other.test(assignment);
    }

    default AssignmentFilter or(AssignmentFilter other) {
        return assignment -> this.test(assignment) || other.test(assignment);
    }
}
