package org.example.sorter;

import org.example.assignment.RoleAssignment;
import java.util.Comparator;

public class AssignmentSorters {

    private AssignmentSorters() {}

    public static Comparator<RoleAssignment> byUsername() {
        return Comparator.comparing(a -> a.user().username());
    }

    public static Comparator<RoleAssignment> byRoleName() {
        return Comparator.comparing(a -> a.role().getName());
    }

    public static Comparator<RoleAssignment> byAssignmentDate() {
        return Comparator.comparing(a -> a.metadata().assignedAt());
    }
}